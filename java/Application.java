import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

public class Application {
    private final int lotsQuantity;
    private final int lotToSkipPunishment;
    private final int lotsInitialPrice;
    private final AuctionMember[] members;

    public Application(int lotsQuantity, int lotToSkipPunishment, int lotsInitialPrice, int membersQuantity) {
        this.lotsQuantity = lotsQuantity;
        this.lotToSkipPunishment = lotToSkipPunishment;
        this.lotsInitialPrice = lotsInitialPrice;
        members = new AuctionMember[membersQuantity];
    }

    void start() {
        Auction a = new Auction(lotsQuantity, lotToSkipPunishment, lotsInitialPrice, members.length);
        Runnable runnable = () -> {
            int winner = a.getWinner();
            System.out.println("The winner of lot №" + a.getCurrentLotIndex() + " is " + winner + " with final price of " + a.getCurrentLotPrice());
            System.out.println("Waiting for money...");
            a.nextLot();
            if (members[winner].payMoney()) {
                System.out.println("The winner paid a money!");
            }
            else {
                System.out.println("The winner DIDN'T pay! Punished.");
                a.punishMember(winner);
            }
            System.out.println();
        };
        CyclicBarrier barrier = new CyclicBarrier(members.length, runnable);
        ReentrantLock lock = new ReentrantLock();

        for (int i=0; i<members.length; i++) {
            members[i] = new AuctionMember(a, i, lock, barrier);
            new Thread(members[i]).start();
        }
    }

    public static void main(String[] args) {
        new Application(10,2,100,4).start();
    }
}
