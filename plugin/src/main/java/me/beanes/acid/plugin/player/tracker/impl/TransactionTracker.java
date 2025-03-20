package me.beanes.acid.plugin.player.tracker.impl;

import com.github.retrooper.packetevents.netty.buffer.UnpooledByteBufAllocationHelper;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public class TransactionTracker extends Tracker {
    private int sent = 0;
    private int received = 0;
    @Getter
    private final long loginTime;
    @Getter
    private long pingTimePassed;
    private final List<TransactionTask> tasks = new LinkedList<>();
    private final List<TransactionResponseTask> responseTasks = new LinkedList<>(); // same as tasks but the task consumes the response time
    private final Deque<Long> sentTimes = new ArrayDeque<>();
    private final Object cachedTransaction;
    private static final long TRANSACTION_TIMEOUT = 30_000L; // Make configurable?

    public TransactionTracker(PlayerData data) {
        super(data);

        this.loginTime = System.currentTimeMillis();

        // Writes the buffer to a netty bytebuf that we can keep reusing to send transactions
        // TODO: make this static
        WrapperPlayServerWindowConfirmation transactionWrapper = new WrapperPlayServerWindowConfirmation(0, (short) -1, false);
        transactionWrapper.buffer = UnpooledByteBufAllocationHelper.buffer();
        transactionWrapper.prepareForSend(data.getUser().getChannel(), true, false);
        transactionWrapper.write();

        cachedTransaction = transactionWrapper.buffer;
    }

    public void sendTransaction() {
        data.getUser().sendPacket(new WrapperPlayServerWindowConfirmation(0, (short) -1, false));
    }

    public void sendTransactionWithEventLoop() {
        ChannelHelper.runInEventLoop(data.getUser().getChannel(), () -> { // TODO: lazy to prevent wakeup
            data.getUser().sendPacket(new WrapperPlayServerWindowConfirmation(0, (short) -1, false));
        });
    }

    public int getLastTransactionSent() {
        return this.sent;
    }

    public void handleServerTransaction(WrapperPlayServerWindowConfirmation wrapper) {
        if (wrapper.getActionId() != -1) {
            return;
        }

        sentTimes.add(System.currentTimeMillis());
        sent++;
    }

    public void handleClientTransaction(WrapperPlayClientWindowConfirmation wrapper) {
        if (wrapper.getActionId() != -1) {
            return;
        }

        int currentTransaction = ++received;

        // Either the client is on a hacking client or a plugin/spigot is messing with transactions (without going through pe)
        if (received > sent) {
            // TODO: report reason to cloud
            data.getUser().closeConnection();
            return;
        }

        long sentTime = sentTimes.pop();
        long responseTime = System.currentTimeMillis() - sentTime;
        if (responseTime > TRANSACTION_TIMEOUT) {
            // TODO: report to cloud that the player took to long to respond to transaction
            data.getUser().closeConnection();
            return;
        }

        this.pingTimePassed = sentTime - loginTime;

        Iterator<TransactionTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            TransactionTask scheduledTask = iterator.next();
            if (scheduledTask.getTransaction() == currentTransaction) {
                iterator.remove();
                scheduledTask.getTask().run();
            }
        }

        Iterator<TransactionResponseTask> otherIterator = responseTasks.iterator();
        while (otherIterator.hasNext()) {
            TransactionResponseTask scheduledTask = otherIterator.next();
            if (scheduledTask.getTransaction() == currentTransaction) {
                otherIterator.remove();
                scheduledTask.getTask().accept((int) responseTime); // We can safely cast to int as a response time over max int would have been a timeout anyways
            }
        }
    }

    public void pre(Runnable runnable) {
        // Runs a runnable when the client accepts the last sent transaction
        this.scheduleTrans(0, runnable);
    }

    public void post(Runnable runnable) {
        // Runs a runnable when the client accepts the next sent transaction
        this.scheduleTrans(1, runnable);
    }

    public void pre(IntConsumer consumer) {
        // Applies response time on a consumer when the client accepts the last sent transaction
        this.scheduleResponseTrans(0, consumer);
    }

    public void post(IntConsumer consumer) {
        // Applies response time on a consumer when the client accepts the next sent transaction
        this.scheduleResponseTrans(1, consumer);
    }

    private void scheduleTrans(int offset, Runnable runnable) {
        int targetTransaction = sent + offset;

        if (received >= targetTransaction) {
            runnable.run();
            return;
        }

        tasks.add(new TransactionTask(targetTransaction, runnable));
    }

    public void scheduleResponseTrans(int offset, IntConsumer task) {
        int targetTransaction = sent + offset;

        if (received >= targetTransaction) {
            task.accept(0);
            return;
        }

        responseTasks.add(new TransactionResponseTask(targetTransaction, task));
    }

    // Check if the player is responding to transactions (on their keep alive packets)
    public void checkTransactionResponseTime() {
        Long firstTransactionQueuedSentTime = sentTimes.peek();


        if (firstTransactionQueuedSentTime != null) {
            long delta = System.currentTimeMillis() - firstTransactionQueuedSentTime;

            if (delta > TRANSACTION_TIMEOUT) {
                // TODO: report to cloud that the player took to long to respond to transaction
                data.getUser().closeConnection();
            }
        }
    }

    @AllArgsConstructor
    @Getter
    static class TransactionTask {
        private int transaction;
        private Runnable task;
    }

    @AllArgsConstructor
    @Getter
    static class TransactionResponseTask {
        private int transaction;
        private IntConsumer task;
    }

}

