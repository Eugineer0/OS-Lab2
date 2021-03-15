import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NonBlockingArrayListTest {
    private static NonBlockingArrayList<Integer> list = new NonBlockingArrayList<>();
    private static int threads_num = 30;

    private static int elements_num;

    private static class TestingThread extends Thread {
        TestingThread() {
        }

        @Override
        public void run() {
            editCollection(list);
        }

        void editCollection(NonBlockingArrayList<Integer> list) {
            Iterator<Integer> iterator = list.iterator();
            while (iterator.hasNext()) {
                Integer element = iterator.next();
                System.out.printf("Thread %d prints element: %d\n", this.getVirtualID(), element);

                if (element == 2) {
                    System.out.printf("\tThread %d removes element: %d successfully: %b\n", this.getVirtualID(), element, list.remove(element)? elements_num-- : null);
                    list.add(3);
                    elements_num++;
                    System.out.printf("\t\tThread %d adds element: 3\n", this.getVirtualID());
                }
                else if (element == 3) {
                    System.out.printf("\tThread %d removes element: %d successfully: %b\n", this.getVirtualID(), element, list.remove(element) ? elements_num-- : null);
                    list.add(2);
                    elements_num++;
                    System.out.printf("\t\tThread %d adds element: 2\n", this.getVirtualID());
                }
            }
        }

        private long getVirtualID()
        {
            return this.getId() - 14;
        }
    }

    static void printCollection(ArrayList<Integer> list) {
        Iterator<Integer> iterator = list.iterator();
        while(iterator.hasNext()){
            int element = iterator.next();
            System.out.printf("  %d %n", element);
        }
    }

    @Test
    public void test() throws InterruptedException {
        list.add(1);
        list.add(2);

        elements_num = list.size();

        ArrayList<TestingThread> threads = new ArrayList<>();

        for(int i = 0; i < threads_num; i++)
        {
            threads.add(new TestingThread());
            threads.get(i).start();
            //threads.get(i).join();
        }

        for(int i = 0; i < threads_num; i++)
        {
            threads.get(i).join();
        }

        System.out.println("\n\nПосле работы потоков");
        printCollection(list);


        assertEquals(elements_num, list.size());
    }
}