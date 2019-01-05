package pl.edu.wat.wcy.jfk.lab2;

import pl.edu.wat.wcy.jfk.lab2.mvc.Application;
import pl.edu.wat.wcy.jfk.lab2.mvc.Controller;
import pl.edu.wat.wcy.jfk.lab2.mvc.Model;

public class Main {
    public static void main(String[] args) {
        Model model = new Model();
        Application view  = new Application();
        Controller controller = new Controller(model, view);
        view.setVisible(true);
    }
}
