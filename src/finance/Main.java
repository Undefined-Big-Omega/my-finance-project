package finance;

import finance.services.AuthService;
import finance.services.FinanceService;
import finance.services.StorageService;
import finance.ui.UITerminal;

public class Main {
    public static void main(String[] args) {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        StorageService storageService = new StorageService();

        UITerminal ui = new UITerminal(authService, financeService, storageService);
        ui.start();
    }
}
