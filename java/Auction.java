import java.util.Arrays;

public class Auction {
    private final int[] lots;
    private final int[] membersStatus;
    private int currentLotI;
    private final int lotToSkipPunishment;
    private int currentWinner;
    private boolean isWon;
    private long lastChangeTime;


    public Auction(int lotsQuantity, int lotToSkipPunishment, int lotsInitialPrice, int membersQuantity) {
        lots = new int[lotsQuantity];
        Arrays.fill(lots, lotsInitialPrice);
        membersStatus = new int[membersQuantity];
        Arrays.fill(membersStatus, 0);
        this.lotToSkipPunishment = lotToSkipPunishment;
        this.currentLotI = 0;
        this.currentWinner = -1;
        this.lastChangeTime = -1;
        this.isWon = false;
    }

    public void nextLot() {
        if (isFinished()) {
            throw new RuntimeException();
        }
        currentWinner = -1;
        lastChangeTime = -1;
        isWon = false;
        currentLotI++;
        for (int i=0; i<membersStatus.length; i++) {
            if (membersStatus[i] != 0) {
                membersStatus[i]--;
            }
        }
    }

    public void punishMember(int memberI) {
        if (memberI > membersStatus.length-1) {
            throw new RuntimeException();
        }
        membersStatus[memberI] = lotToSkipPunishment;
    }

    public boolean setNewLotPrice(int memberI, int newPrice) {
        if (newPrice <= lots[currentLotI] || membersStatus[memberI] > 0) {
            throw new RuntimeException();
        }

        if (lastChangeTime == -1) {
            lastChangeTime = System.currentTimeMillis();
        }
        else if (lastChangeTime + 3000 < System.currentTimeMillis()) {
            isWon = true;
            return false;
        }

        lastChangeTime = System.currentTimeMillis();
        lots[currentLotI] = newPrice;
        currentWinner = memberI;
        return true;
    }

    public int getCurrentLotPrice() {
        return lots[currentLotI];
    }

    public int getWinner() {
        return currentWinner;
    }

    public int getCurrentLotIndex() {
        return currentLotI;
    }

    public int membersQuantity() {
        return membersStatus.length;
    }

    public boolean isFinished() {
        return currentLotI >= lots.length;
    }

    public boolean isMemberPunished(int memberI) {
        return membersStatus[memberI] > 0;
    }

    public boolean isLotWon() {
        return isWon;
    }
}
