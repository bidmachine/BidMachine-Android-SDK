package io.bidmachine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
abstract class MultiThreadingTest {

    static Collection<Object[]> createDefaultParameters() {
        return Arrays.asList(new Object[][]{
                {100, 2},
                {200, 3},
                {100, 4},
                {300, 5},
                {150, 6},
                {400, 7}
        });
    }

    private final int actionCount;
    private final int readThreadCount;

    private final AtomicInteger countSuccess;

    public MultiThreadingTest(int actionCount, int readThreadCount) {
        this.actionCount = actionCount;
        this.readThreadCount = readThreadCount;

        countSuccess = new AtomicInteger(0);
    }

    @Test(timeout = 60000)
    public void test() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(readThreadCount + 1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 1; i <= actionCount; i++) {
                        writeAction(i, actionCount);
                        try {
                            Thread.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    countSuccess.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        }).start();

        for (int i = 0; i < readThreadCount; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int totalActions = actionCount / readThreadCount;
                        for (int i = 1; i <= totalActions; i++) {
                            readAction(i, totalActions);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        countSuccess.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    countDownLatch.countDown();
                }
            }).start();
        }
        countDownLatch.await();

        assertEquals(readThreadCount + 1, countSuccess.get());
    }

    abstract void writeAction(int actionNumber, int totalActions) throws Exception;

    abstract void readAction(int actionNumber, int totalActions) throws Exception;

}
