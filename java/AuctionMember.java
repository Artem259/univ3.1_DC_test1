import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionMember implements Runnable {
    Auction auction;
    private final int memberI;
    private final ReentrantLock lock;
    CyclicBarrier barrier;
    Random random;

    public AuctionMember(Auction auction, int memberI, ReentrantLock lock, CyclicBarrier barrier) {
        this.auction = auction;
        this.memberI = memberI;
        this.lock = lock;
        this.barrier = barrier;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (!auction.isFinished()) {
            while (!auction.isLotWon()) {
                try {
                    lock.lock(); // >>>
                    if (!auction.isMemberPunished(memberI)) {
                        int oldPrice = auction.getCurrentLotPrice();
                        int flag = random.nextInt(auction.membersQuantity());
                        if (flag == 0) {
                            int newPrice = oldPrice + (1 + random.nextInt(5));
                            if (auction.setNewLotPrice(memberI, newPrice)) {
                                System.out.println("Member " + memberI + " set new price: " + newPrice);
                            }
                        }
                    }
                }
                finally {
                    lock.unlock(); // <<<
                }

                //System.out.println(memberI);
                try {
                    Thread.sleep(1000 + random.nextInt(1500));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                //System.out.println(" > " + memberI);
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean payMoney() {
        int timeOut = 1000;
        int timeToThink = random.nextInt(timeOut*2);
        try {
            Thread.sleep(timeToThink);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return timeToThink < timeOut;
    }
}
