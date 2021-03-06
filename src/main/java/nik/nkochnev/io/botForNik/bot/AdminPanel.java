package nik.nkochnev.io.botForNik.bot;

import lombok.RequiredArgsConstructor;
import nik.nkochnev.io.botForNik.model.Auction;
import nik.nkochnev.io.botForNik.model.Payment;
import nik.nkochnev.io.botForNik.model.User;
import nik.nkochnev.io.botForNik.model.Winner;
import nik.nkochnev.io.botForNik.service.AuctionServiceImpl;
import nik.nkochnev.io.botForNik.service.PaymentServiceImpl;
import nik.nkochnev.io.botForNik.service.UserServiceImpl;
import nik.nkochnev.io.botForNik.service.WinnerService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminPanel {

    private final List<KeyboardRow> keyboard = new ArrayList<>();
    private final UserServiceImpl userService;
    private final AuctionServiceImpl auctionService;
    private final PaymentServiceImpl paymentService;
    private final WinnerService winnerService;

    private final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

    public SendMessage start(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getFrom().getId()));
        sendMessage.setText("Админ-панель:");

        return adminMenu(sendMessage);
    }

    public SendMessage adminMenu(SendMessage sendMessage){
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton("Добавить аукцион");
        keyboardRow.add(keyboardButton);

        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardButton keyboardButton1 = new KeyboardButton("Список пополнений");
        keyboardRow1.add(keyboardButton1);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardButton keyboardButton2 = new KeyboardButton("Список пользователей");
        keyboardRow2.add(keyboardButton2);

        KeyboardRow keyboardRow4 = new KeyboardRow();
        KeyboardButton keyboardButton4 = new KeyboardButton("Список победителей");
        keyboardRow4.add(keyboardButton4);

        KeyboardRow keyboardRow5 = new KeyboardRow();
        KeyboardButton keyboardButton5 = new KeyboardButton("Выдать деньги");
        keyboardRow5.add(keyboardButton5);

        KeyboardButton keyboardButton6 = new KeyboardButton("Заблокировать");
        keyboardRow5.add(keyboardButton6);

        KeyboardRow keyboardRow3 = new KeyboardRow();
        KeyboardButton keyboardButton3 = new KeyboardButton("Выйти в меню");
        keyboardRow3.add(keyboardButton3);

        keyboard.add(keyboardRow);
        keyboard.add(keyboardRow1);
        keyboard.add(keyboardRow2);
        keyboard.add(keyboardRow4);
        keyboard.add(keyboardRow5);
        keyboard.add(keyboardRow3);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(keyboardMarkup);

        return sendMessage;
    }

    public SendMessage addAuction(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getFrom().getId()));
        sendMessage.setText("Введите данные:\n" +
                "[Название приза] [стартовая сумма] \n(ЧЕРЕЗ ПРОБЕЛ)");

        Optional<User> admin = userService.findById(update.getMessage().getFrom().getId());
        if (admin.isEmpty()){
            sendMessage.setText("Ошибка какая-то");
            return sendMessage;
        }
        final var user = admin.get();
        user.setPosition("add_auction");
        userService.save(user);

        return sendMessage;
    }

    public List<SendMessage> addAuctionImpl(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getFrom().getId()));
        String text = update.getMessage().getText();
        Optional<User> user = userService.findById(update.getMessage().getFrom().getId());
        final var admin = user.get();

        List<SendMessage> messages = new ArrayList<>();

        final var s = text.split(" ");
        if (s.length > 2){
            sendMessage.setText("Неправильно введены данные");
            admin.setPosition("start");
            userService.save(admin);

            messages.add(sendMessage);
            return messages;
        }

        String prizeName = s[0];
        int prizeSum = Integer.parseInt(s[1]);

        Auction auction = new Auction();
        auction.setThingName(prizeName);
        auction.setStartSum(prizeSum);
        auctionService.save(auction);

        admin.setPosition("start");
        userService.save(admin);

        final var all = userService.findAll();

        String textForUsers = "Создан новый лот: " + auction.getThingName();

        for (User u : all){
            sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(u.getUserId()));
            sendMessage.setText(textForUsers);
            messages.add(sendMessage);
        }

        return messages;
    }

    public SendMessage usersToday(Update update){
        final var allByLastActionBetween =
                userService.findAllByLastActionBetween(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getFrom().getId()));

        StringBuilder sb = new StringBuilder();
        sb.append("Список юзеров за 24ч: \n");

        if (allByLastActionBetween.isEmpty()){
            sendMessage.setText("Юзеров за 24ч нет");
            return sendMessage;
        }

        for (User us : allByLastActionBetween){
            sb.append("Id: ").append(us.getUserId())
                    .append(" | ").append("Email: ").append(us.getEmail())
                    .append(" | ").append("Money: ").append(us.getMoney())
                    .append("\n");
        }
        sendMessage.setText(sb.toString());
        return sendMessage;
    }

    public SendMessage allGetMoney(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getFrom().getId()));

        final var paymentByPaymentDate =
                paymentService.findPaymentByPaymentDate(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        if (paymentByPaymentDate.isEmpty()){
            sendMessage.setText("Пополнений за 24ч нет");
            return sendMessage;
        }

        StringBuilder sb = new StringBuilder();
        for (Payment payment : paymentByPaymentDate){
            sb.append("UserId: ").append(payment.getUser().getUserId())
                    .append(" Money: ").append(payment.getMoney())
                    .append(" Time: ").append(payment.getPaymentDate()).append("\n");
        }

        sendMessage.setText(sb.toString());
        return sendMessage;
    }

    public SendMessage winners(Update update){
        String userId = String.valueOf(update.getMessage().getFrom().getId());
        List<Winner> winners = winnerService.findWinnerByPrized(false);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);

        if (winners.isEmpty()){
            sendMessage.setText("Все призы выданы");
            return sendMessage;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Введите id, кому выдали приз\n");
        for (Winner w : winners){
            sb.append("Id: ").append(w.getWinnerId()).append(" Username: ").append(w.getUser().getUsername())
                    .append(" Вещь: ").append(w.getAuction().getThingName()).append("\n");
        }
        sendMessage.setText(sb.toString());

        Optional<User> user = userService.findById(Integer.valueOf(userId));
        final var user1 = user.get();
        user1.setPosition("prized");
        userService.save(user1);

        return exit(sendMessage);
    }

    public SendMessage winnersImpl(Update update){
        String userId = String.valueOf(update.getMessage().getFrom().getId());
        String winnerId = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);

        final var byId = winnerService.findById(Integer.valueOf(winnerId));
        if (byId.isEmpty()){
            sendMessage.setText("Все призы выданы");
            return sendMessage;
        }

        final var winner = byId.get();
        winner.setPrized(true);
        winnerService.save(winner);

        User user = userService.findById(Integer.valueOf(userId)).get();
        user.setPosition("start");
        userService.save(user);

        sendMessage.setText("Приз выдан");
        return sendMessage;
    }

    public SendMessage addMoney(Update update){
        String userId = String.valueOf(update.getMessage().getFrom().getId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setText("Введите через пробел сумму и username \n" +
                "Пример: 1000 hypelil");

        User user = userService.findById(Integer.valueOf(userId)).get();
        user.setPosition("add_money");
        userService.save(user);

        return sendMessage;
    }

    public SendMessage addMoneyImpl(Update update){
        String userId = String.valueOf(update.getMessage().getFrom().getId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);

        User admin = userService.findById(Integer.valueOf(userId)).get();

        String money = update.getMessage().getText().split(" ")[0];
        String username = update.getMessage().getText().split(" ")[1];

        User user = userService.findByUsername("@" + username);
        user.setMoney(user.getMoney() + Double.parseDouble(money));
        userService.save(user);

        admin.setPosition("start");
        userService.save(admin);

        sendMessage.setText("Деньги зачислены");

        return adminMenu(sendMessage);
    }

    public SendMessage ban(Update update){
        String userId = String.valueOf(update.getMessage().getFrom().getId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);

        User admin = userService.findById(Integer.valueOf(userId)).get();
        admin.setPosition("ban");
        userService.save(admin);

        sendMessage.setText("Введите username\n" +
                "Пример: hypelil");
        return sendMessage;
    }

    public SendMessage banImpl(Update update){
        String userId = String.valueOf(update.getMessage().getFrom().getId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);

        User admin = userService.findById(Integer.valueOf(userId)).get();
        admin.setPosition("start");
        userService.save(admin);

        String userIdForBan = update.getMessage().getText();
        User user = userService.findByUsername("@" + userIdForBan);

        if (user == null){
            sendMessage.setText("Юзер не найден");
            return sendMessage;
        }

        System.out.println(user.getUserId());
        user.setBanned(true);
        userService.save(user);

        sendMessage.setText("Юзер забанен");
        return sendMessage;
    }

    public SendMessage exit(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getFrom().getId()));
        sendMessage.setText("Вы вышли из админ-панели");
        return sendMessage;
    }

    public SendMessage exit(SendMessage sendMessage){
        KeyboardRow keyboardRow3 = new KeyboardRow();
        KeyboardButton keyboardButton3 = new KeyboardButton("Назад");
        keyboardRow3.add(keyboardButton3);

        keyboard.add(keyboardRow3);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(keyboardMarkup);

        return sendMessage;
    }
}
