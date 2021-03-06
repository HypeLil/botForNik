package nik.nkochnev.io.botForNik.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nik.nkochnev.io.botForNik.model.Auction;
import nik.nkochnev.io.botForNik.model.Participant;
import nik.nkochnev.io.botForNik.model.User;
import nik.nkochnev.io.botForNik.model.Winner;
import nik.nkochnev.io.botForNik.service.AuctionServiceImpl;
import nik.nkochnev.io.botForNik.service.ParticipantServiceImpl;
import nik.nkochnev.io.botForNik.service.UserServiceImpl;
import nik.nkochnev.io.botForNik.service.WinnerService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String token;

    @Value("$bot.name")
    private String name;

    @Value("${bot.admin}")
    private String admin;

    private final int merchantId = 100;

    private final UserServiceImpl userService;
    private final AdminPanel adminPanel;
    private final AuctionServiceImpl auctionService;
    private final ParticipantServiceImpl participantService;
    private final WinnerService winnerService;

    private List<KeyboardRow> keyboard = new ArrayList<>();
    private final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    private final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()){
            int userId;

            log.info("message {}", update.getMessage().getText());
            userId = Math.toIntExact(update.getMessage().getFrom().getId());
            Optional<User> user = userService.findById(userId);
            if (user.isEmpty()){
                User us = new User(userId);
                if (update.getMessage().getFrom().getUserName() != null){
                    us.setUsername("@" + update.getMessage().getFrom().getUserName());
                }
                else us.setUsername(String.valueOf(update.getMessage().getFrom().getId()));
                userService.save(us);
            } else {
                final var user1 = user.get();

                if (user1.isBanned()){
                    return;
                }

                user1.setLastAction(LocalDateTime.now());
                userService.save(user1);
            }

            handleMessage(userId, update);
        }

        if (update.hasCallbackQuery()){
            int userId = update.getCallbackQuery().getFrom().getId();
            String command = update.getCallbackQuery().getData();
            userId = Math.toIntExact(update.getCallbackQuery().getFrom().getId());
            if (command.split("_").length > 1
                    && command.split("_")[0].equals("auction")){
                sendMessage(auctionImpl(update));
            }
            else if (command.split("_").length > 1
                    && command.split("_")[0].equals("participant")){
                sendMessage(participantImpl(update));
            }
        }
    }

    public void handleMessage(int userId, Update update){
        Optional<User> user = userService.findById(userId);

        if ("Назад".equalsIgnoreCase(update.getMessage().getText()) &&
                admin.equalsIgnoreCase(String.valueOf(update.getMessage().getFrom().getId()))){
            final var user1 = user.get();
            user1.setPosition("start");
            userService.save(user1);

            sendMessage(adminPanel.start(update));
        }

        if (user.isPresent()){
            if ("payment".equalsIgnoreCase(user.get().getPosition())){
                paymentImpl(user.get(), update);
            }
            else if ("email".equalsIgnoreCase(user.get().getPosition())){
                createStartMenu(email(update));
            }
            else if ("prized".equalsIgnoreCase(user.get().getPosition())){
                sendMessage(adminPanel.winnersImpl(update));
            }
            else if ("add_auction".equalsIgnoreCase(user.get().getPosition())){
                adminPanel.addAuctionImpl(update).forEach(this::sendMessage);
            }
            else if("add_money".equalsIgnoreCase(user.get().getPosition())){
                sendMessage(adminPanel.addMoneyImpl(update));
            }
            else if("ban".equalsIgnoreCase(user.get().getPosition())){
                sendMessage(adminPanel.banImpl(update));
            }
        } else {
            User us = new User(userId);
            us.setUsername(update.getMessage().getFrom().getUserName());
            userService.save(us);
        }

        String text = update.getMessage().getText();
        if ("/start".equals(text)){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(userId));
            sendMessage.enableMarkdown(true);

            User us = userService.findById(userId).get();

            if (us.getEmail() == null){
                sendMessage.setText("Добро пожаловать в бот аукцион\n" +
                        "Введите, пожалуйста, свой email" +
                        "\n\nБудьте внимательны! Потом сменить нельзя\n" +
                        "Используется при пополнении");
               us.setPosition("email");
               userService.save(us);
               log.info("Email will be changed");
               sendMessage(sendMessage);
               return;
            }

            else sendMessage.setText("Добро пожаловать в бот аукцион");

            createStartMenu(sendMessage);
        }
        else if ("Пополнение кошелька".equalsIgnoreCase(text)){
            payment(userId);
        }
        else if ("Правила".equalsIgnoreCase(text)){
            rules(update);
        }
        else if ("Аукционы".equalsIgnoreCase(text)){
            auctions(update);
        }
        else if ("Выйти в меню".equalsIgnoreCase(text)){
            createStartMenu(adminPanel.exit(update));
        }
        else if ("Админ-панель".equals(text)){
            if (admin.equals(String.valueOf(update.getMessage().getFrom().getId()))){
                sendMessage(adminPanel.start(update));
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(userId));
                sendMessage.enableMarkdown(true);
                sendMessage.setText("Доступ запрещен");
                sendMessage(sendMessage);
            }
        }
        else if ("Список пользователей".equals(text)){
            if (admin.equals(String.valueOf(update.getMessage().getFrom().getId()))){
                sendMessage(adminPanel.usersToday(update));
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(userId));
                sendMessage.enableMarkdown(true);
                sendMessage.setText("Доступ запрещен");
                sendMessage(sendMessage);
            }
        }
        else if ("Добавить аукцион".equals(text)){
            if (admin.equals(String.valueOf(update.getMessage().getFrom().getId()))){
                sendMessage(adminPanel.addAuction(update));
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(userId));
                sendMessage.enableMarkdown(true);
                sendMessage.setText("Доступ запрещен");
                sendMessage(sendMessage);
            }
        }
        else if ("Список пополнений".equals(text)){
            if (admin.equals(String.valueOf(update.getMessage().getFrom().getId()))){
                sendMessage(adminPanel.allGetMoney(update));
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(userId));
                sendMessage.enableMarkdown(true);
                sendMessage.setText("Доступ запрещен");
                sendMessage(sendMessage);
            }
        }
        else if ("Список победителей".equals(text)){
            if (admin.equals(String.valueOf(update.getMessage().getFrom().getId()))){
                sendMessage(adminPanel.winners(update));
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(userId));
                sendMessage.enableMarkdown(true);
                sendMessage.setText("Доступ запрещен");
                sendMessage(sendMessage);
            }
        }
        else if ("Выдать деньги".equals(text)){
            if (admin.equals(String.valueOf(update.getMessage().getFrom().getId()))){
                sendMessage(adminPanel.addMoney(update));
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(userId));
                sendMessage.enableMarkdown(true);
                sendMessage.setText("Доступ запрещен");
                sendMessage(sendMessage);
            }
        }
        else if ("Заблокировать".equals(text)){
            if (admin.equals(String.valueOf(update.getMessage().getFrom().getId()))){
                sendMessage(adminPanel.ban(update));
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(userId));
                sendMessage.enableMarkdown(true);
                sendMessage.setText("Доступ запрещен");
                sendMessage(sendMessage);
            }
        }
    }

    public SendMessage email(Update update){
        String email = update.getMessage().getText();
        User user = userService.findById(Math.toIntExact(update.getMessage().getFrom().getId())).get();
        user.setEmail(email);
        user.setPosition("start");
        userService.save(user);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getFrom().getId()));
        sendMessage.enableMarkdown(true);
        sendMessage.setText("Email удачно сменен");
        return sendMessage;
    }

    public void sendMessage(SendMessage sendMessage){
        try {
            execute(sendMessage);
        } catch (TelegramApiException telegramApiException) {
           log.error("Error when try sending message {}", sendMessage.getText());
        }
    }

    public void createStartMenu(SendMessage sendMessage){
        keyboard = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton("Аукционы");
        keyboardRow.add(keyboardButton);

        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardButton keyboardButton1 = new KeyboardButton("Правила");
        keyboardRow1.add(keyboardButton1);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardButton keyboardButton2 = new KeyboardButton("Пополнение кошелька");
        keyboardRow2.add(keyboardButton2);

        keyboard.add(keyboardRow);
        keyboard.add(keyboardRow1);
        keyboard.add(keyboardRow2);

        if (sendMessage.getChatId().equals(admin)){

            KeyboardRow keyboardRow3 = new KeyboardRow();
            KeyboardButton keyboardButton3 = new KeyboardButton("Админ-панель");
            keyboardRow3.add(keyboardButton3);

            keyboard.add(keyboardRow3);
        }

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(keyboardMarkup);

        sendMessage(sendMessage);
    }

    public void payment(int userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(userId));
        sendMessage.enableMarkdown(true);
        sendMessage.setText("Введите сумму, на которую хотите пополнить счет");

        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) userService.save(new User(userId));
        User user = userOptional.get();

        user.setPosition("payment");
        userService.save(user);
        log.info("Юзер {} пополняет счет", userId);

        sendMessage(sendMessage);
    }

    public void paymentImpl(User user, Update update){
        String text = update.getMessage().getText();
        int sum = 0;
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(user.getUserId()));
        sendMessage.enableMarkdown(true);
        sendMessage.setText("Нажмите на кнопку для пополнения:");

        try {
            sum = Integer.parseInt(text);
        } catch (Exception e){
            sendMessage.setText("Вы ввели не число");
            sendMessage(sendMessage);
            log.error("Неправильно введена сумма");
        }
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

        inlineKeyboardButton.setText("Пополнить на " + sum + " рублей");
        inlineKeyboardButton.setUrl("https://www.free-kassa.ru/merchant/cash.php?m=" + merchantId +
                "&oa=" + sum + "&o=" + user.getUserId() + "&s="+getMd5(sum, user.getUserId())+"&lang=ru&us_type=rub&type=json");
        row.add(inlineKeyboardButton);
        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        user.setPosition("start");
        userService.save(user);

        sendMessage(sendMessage);
    }

    @SneakyThrows
    private String getMd5(int sum, int userId){
        String secretWord = "12345godiblack1234";
        String label = merchantId + ":" + sum +":" + secretWord + ":" + userId;
        return DigestUtils.md5Hex(label);
    }

    public void rules(Update update){
        int userId = Math.toIntExact(update.getMessage().getFrom().getId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(userId));
        sendMessage.enableMarkdown(true);
        sendMessage.setDisableWebPagePreview(true);

        sendMessage.setText("Правила аукциона \n" +
                "\n" +
                "⛔️ Запрещено:\n" +
                "1. Писать другим участникам аукциона оскорбительные сообщения, количество своих аукционных ставок, запугивания, а также собираться в группы.\n" +
                "2) Создавать несколько аккаунтов.\n" +
                "3) Передавать свой аккаунт другим лицам.\n" +
                "4) Допускается только 1 отказ от покупки лота после выигрыша.\n" +
                "5) Использовать программный софт для автоматической перебивки лотов (Автокликеры). При нарушении данного пункта лот может быть аннулирован! ❗️\n" +
                "\n" +
                "Лимит обнуляется в 00:00 по мск времени \uD83D\uDD25\n" +
                "\n" +
                "В случае если после нарушений вы одерживаете победу, победа над лотом считается «не состоявшейся» и лот будет выставлен на повторные торги.\n" +
                "\n" +
                "▫️В случае обнаружения таких нарушений ваш аккаунт может попасть в Бан от 1-3 дней.\n" +
                "При этом аукционные ставки на балансе возврату не подлежат.\n" +
                "\n" +
                "▫️Если вы заметили нарушение, просьба незамедлительно сообщить в Тех.поддержку .\n" +
                "\n" +
                "\n" +
                "❓ Часто задаваемые вопросы:\n" +
                "\n" +
                "1. Являются ли товары новыми и оригинальными?\n" +
                "\n" +
                "✅ Все лоты, выставляемые на торги в нашем аукционе являются оригинальными и абсолютно новыми, такие же, как вы могли бы приобрести у производителя, либо дилера, только со скидкой до 99%.\n" +
                "\n" +
                "2. Почему оригинальный товар стоит так дёшево?\n" +
                "\n" +
                "✅ Концепция бота предусматривает оплату за участие в аукционах, за счёт которой компенсируется основная часть стоимости товара, поскольку в аукционе участвует большое количество пользователей, а лот по итогам торгов забирает только один пользователь - победитель.\n" +
                "\n" +
                "\n" +
                "3. Если ставка не сыграла - она «сгорает»?\n" +
                "\n" +
                "✅ Если ставка пользователя была перебита другим участником аукциона, пользователь может сделать новую ставку.\n" +
                "\n" +
                "4. Я случайно сделал ставку, могу ли отменить её?\n" +
                "\n" +
                "✅ Сделанные ставки отменить невозможно, поэтому рекомендуем участвовать только в тех аукционах, которые проводятся по интересующим вас лотам.\n" +
                "\n" +
                "5. Возможен ли обмен или возврат товара?\n" +
                "✅ Возврат или обмен товара, купленного победителем аукциона, возможен в случае выявления каких-либо скрытых дефектов или брака в момент получения товара от перевозчика.\n" +
                "\n" +
                "6. Оплатил, но ставки не добавились. Что делать?\n" +
                "✅ Оплата производится с помощью системы электронных платежей, обычно это не занимает много времени, и ставки добавляются в течение от нескольких секунд до 15 минут, если деньги с вашего баланса списались, а ставки не были добавлены в течение 15 минут, обратитесь тех.поддержку IMPERIAL.\n" +
                "\n" +
                "Не нашли ответ на вопрос? \n" +
                "[Обратитесь в тех. поддержку](http://t.me/OptimalniySupport) \uD83D\uDD25");

        try {
            execute(sendMessage);
        } catch (TelegramApiException telegramApiException) {
            telegramApiException.printStackTrace();
        }
    }

    public void auctions(Update update){
        int userId = Math.toIntExact(update.getMessage().getFrom().getId());

        List<Auction> auctionList = auctionService.findAllActualAuctions();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(userId));
        sendMessage.enableMarkdown(true);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton inlineKeyboardButton;

        if (auctionList.isEmpty()){
            sendMessage.setText("Активных аукционов нет \n");
            sendMessage(sendMessage);
            return;
        }

        for (Auction auction : auctionList){
            row = new ArrayList<>();
            inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText("(" + auction.getAuctionId() + ") " + auction.getThingName());
            inlineKeyboardButton.setCallbackData("auction_" + auction.getAuctionId());
            row.add(inlineKeyboardButton);
            keyboard.add(row);
        }
        inlineKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setText("Список активных аукционов: \n");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage(sendMessage);
    }

    public SendMessage auctionImpl(Update update){
        int userId = Math.toIntExact(update.getCallbackQuery().getFrom().getId());
        String auctionId = update.getCallbackQuery().getData().split("_")[1];

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(userId));
        sendMessage.enableMarkdown(true);

        Optional<Auction> auctionOptional = auctionService.findById(Integer.valueOf(auctionId));
        if (auctionOptional.isEmpty()){
            sendMessage.setText("Такого аукциона не существует");
            return sendMessage;
        }

        Auction auction = auctionOptional.get();

        List<Participant> participants = participantService.findByAuction(auction);
        String bets = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (participants.isEmpty()) bets = "Ставок еще нет";
        else {
            StringBuilder sb = new StringBuilder();
            final var collect = participants.stream()
                    .sorted(Comparator.comparing(Participant::getBetTime))
                    .collect(Collectors.toList());
            Collections.reverse(collect);
            final var limit = collect.stream()
                    .limit(10).collect(Collectors.toList());

            for (Participant p : limit) {
                sb.append(p.getBetTime().format(formatter)).append(" - ")
                        .append(p.getUser().getUsername())
                        .append("\n");
            }
            bets = sb.toString();
        }

        long minutes = auction.getSeconds()/60;
        String mins = "" + minutes;
        if (minutes < 10){
            mins = "0" + mins;
        }
        long seconds = auction.getSeconds() % 60;
        String secs = "" + seconds;

        if (seconds < 10){
            secs = "0" + secs;
        }

        StringBuilder sb = new StringBuilder();
                sb.append("#").append(auction.getAuctionId()).append(" ").append(auction.getThingName());
        sb
                .append("\nУ нас исключительно оригинальная техника")
                .append("\nТовар отправляем во все страны")
                .append("\n\uD83D\uDCCDУчаствуй, чтобы победить")
                .append("\n\uD83D\uDCB5Ваш баланс: ").append(userService.findById(userId).get().getMoney())
                .append("\n\nВремя начала: ").append(auction.getStartDate().format(formatter))
                .append("\n\uD83D\uDCB0Текущая цена: ").append(auction.getStartSum())
                .append("\n⏳Остаток времени: ").append(mins).append(":").append(secs)
                .append("\n[Обновление раз в 10 секунд]")
                .append("\n\n----------------------");

                if (!participants.isEmpty()){
                    sb.append("\n\uD83D\uDC51Лидер: ").append(participantService
                            .findByAuctionLeader(auction.getAuctionId()).getUser().getUsername());
                } else sb.append("\n\uD83D\uDC51Лидер: лидера нет");

                sb.append("\nПредыдущие ставки:").append("\n").append(bets);

        sendMessage.setText(sb.toString());

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Ставка");
        inlineKeyboardButton.setCallbackData("participant_" + auction.getAuctionId());

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Обновить");
        inlineKeyboardButton1.setCallbackData("auction_" + auctionId);

        row.add(inlineKeyboardButton);
        row.add(inlineKeyboardButton1);

        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage participantImpl(Update update) {
        SendMessage sendMessage = new SendMessage();
        String auctionId =  update.getCallbackQuery().getData().split("_")[1];
        String userId = String.valueOf(update.getCallbackQuery().getFrom().getId());

        sendMessage.setChatId(userId);
        Optional<User> user = userService.findById(Integer.parseInt(userId));
        final var user1 = user.get();

        Optional<Auction> auction = auctionService.findById(Integer.valueOf(auctionId));

        if (auction.isEmpty()){
            user1.setPosition("start");
            userService.save(user1);
            sendMessage.setText("Ошибка");
            return sendMessage;
        }

        final var auction1 = auction.get();
        int sum = 100;

        if (user1.getMoney() < sum){
            user1.setPosition("start");
            userService.save(user1);
            sendMessage.setText("У вас недостаточно на счете!\n" +
                    "Не хватает: " + (sum - user1.getMoney()));
            return sendMessage;
        }

        Participant participantLeader = participantService.findByAuctionLeader(auction1.getAuctionId());
        if (participantLeader.getUser().getUserId() == Integer.parseInt(userId)){
            sendMessage.setText("Вы лидер и так!");
            return sendMessage;
        }

        Participant participant = new Participant();
        participant.setUser(user1);
        participant.setBetMoney(sum);
        participant.setAuction(auction1);
        participant.setBetTime(LocalDateTime.now());
        participantService.save(participant);

        user1.setMoney(user1.getMoney() - sum);

        auction1.setStartSum(sum);
        auction1.setSeconds(180);
        auction1.setLastBet(LocalDateTime.now());
        auctionService.save(auction1);

        userService.save(user1);

        sendMessage.setText("Вы поставили " + sum + "\n У вас на счете: " + user1.getMoney());
        return sendMessage;
    }

    @Scheduled(fixedDelay = 10000)
    public void plusSecondsInAuction() {
        List<Auction> auctionList = auctionService.findAllActualAuctions();
        if (auctionList.size() >= 1) {
            auctionList
                    .forEach(auction -> {
                        auction.setSeconds(auction.getSeconds() - 10);
                        auctionService.save(auction);
                        if (auction.getSeconds() <= 0) {
                            auction.setEnded(true);
                            auctionService.save(auction);
                            Participant participant = participantService.findByAuctionLeader(auction.getAuctionId());

                            if (participant != null) {
                                auction.setWinnerId(participant.getUser().getUserId());
                                auctionService.save(auction);

                                Winner winner = new Winner();
                                winner.setAuction(auction);
                                winner.setUser(userService.findById(participant.getUser().getUserId()).get());
                                winner.setPrized(false);
                                winnerService.save(winner);

                                SendMessage sendMessage = new SendMessage();
                                sendMessage.setText("Вы победили в аукционе #" + auction.getAuctionId());
                                sendMessage.setChatId(String.valueOf(participant.getUser().getUserId()));
                                sendMessage(sendMessage);
                            }
                            final var byAuction = participantService.findByAuction(auction);

                            if (!byAuction.isEmpty()) {
                                for (Participant p : byAuction) {
                                    final var user = p.getUser();
                                    if (user.getUserId() != auction.getWinnerId()) {
                                        user.setMoney(user.getMoney() + p.getBetMoney());
                                        userService.save(user);
                                    }
                                }
                            }
                        }
                    });
        }
    }
}
