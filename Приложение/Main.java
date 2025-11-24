

public class Main {
    public static void main(String[] args) {
        try {
            DataBase.begin();
        } 
        catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }


        App app = new App();

        while(!app.init());

        while (app.isVisible()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        try {
            DataBase.end();
        } 
        catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

    }
}
