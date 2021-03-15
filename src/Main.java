import java.util.ArrayList;
import java.util.Iterator;

public class Main {
    private static NonBlockingArrayList<String> list = new NonBlockingArrayList<String>();
    private static ArrayList<String> list1 = new ArrayList<>();

    private static class TestThread extends Thread {
        private boolean change;

        TestThread(boolean change) {
            this.change = change;
        }

        @Override
        public void run() {
            if(change) {
                changeCollection(list);
            }
            else {
                printCollection(list);
            }
        }

        void printCollection(NonBlockingArrayList<String> list) {
            Iterator<String> iterator = list.iterator();
            while(iterator.hasNext()){
                String element = iterator.next();
                System.out.printf("Thread ID: %d  Element of collection: %s \n", this.getId(), element);
            }
        }

        void changeCollection(NonBlockingArrayList<String> list) {
            Iterator<String> iterator = list.iterator();
            while(iterator.hasNext()){
                String element = iterator.next();
                if (element.equals("kak")) {
                    list.add("horosho");
                    list.remove(element);
                }
            }
        }
    }



    static void printCollection(boolean change, ArrayList<String> list) {
        Iterator<String> iterator = list.iterator();
        while(iterator.hasNext()){
            String element = iterator.next();
            System.out.printf("  %s %n", element);
            if (change) {
                if (element.equals("kak")) {
                    list.add("horosho");
                    list.remove(element);
                }
            }
        }
    }

    static public void main(String args[]) throws InterruptedException {
        list.add("privet");
        list.add("menya");
        list.add("zovut");
        list.add("afanasiy");
        list.add("kak");
        list.add("dela");

        TestThread change_thread = new TestThread(true);
        TestThread print_thread = new TestThread(false);

        TestThread change_thread1 = new TestThread(true);
        TestThread print_thread1 = new TestThread(false);

        print_thread.start();
        change_thread.start();
        print_thread1.start();

        print_thread.join();
        change_thread.join();
        print_thread1.join();

        System.out.println("\n\nПосле работы потоков");
        printCollection(false, list);


        list1.add("privet");
        list1.add("menya");
        list1.add("zovut");
        list1.add("afanasiy");
        list1.add("kak");
        list1.add("dela");

        try {
            System.out.println("\n\nПопытка одновременного доступа и редактирования стандартной коллекции");
            printCollection(true, list1);
        }catch (Exception e){
            System.out.printf("Standard list throws exceptions at the same task!");
        }
    }

}
