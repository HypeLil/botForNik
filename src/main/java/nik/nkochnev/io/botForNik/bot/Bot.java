package nik.nkochnev.io.botForNik.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nik.nkochnev.io.botForNik.model.User;
import nik.nkochnev.io.botForNik.service.UserServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final List<KeyboardRow> keyboard = new ArrayList<>();
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
        int userId = 0;

        if (update.hasMessage()){
            userId = Math.toIntExact(update.getMessage().getFrom().getId());
            handleMessage(userId, update);
        }

        if (update.hasCallbackQuery()){
            String command = update.getCallbackQuery().getData();
            userId = Math.toIntExact(update.getCallbackQuery().getFrom().getId());
        }
    }

    public void handleMessage(int userId, Update update){
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()){
            if ("payment".equalsIgnoreCase(user.get().getPosition())){
                paymentImpl(user.get(), update);
            }
        }
        String text = update.getMessage().getText();
        if ("/start".equals(text)){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(userId));
            sendMessage.enableMarkdown(true);
            sendMessage.setText("Добро пожаловать в бот аукцион");
            sendMessage(sendMessage);

            createStartMenu(userId);
        }
        else if ("Пополнение кошелька".equals(text)){
            payment(userId);
        }
    }

    public void sendMessage(SendMessage sendMessage){
        try {
            execute(sendMessage);
        } catch (TelegramApiException telegramApiException) {
           log.error("Error when try sending message {}", sendMessage.getText());
        }
    }

    public void createStartMenu(int userId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(userId));
        sendMessage.enableMarkdown(true);

        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton("Аукцион");
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

        keyboardMarkup.setKeyboard(keyboard);
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
}
