import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class LamportMessage {
    public final String content;
    public final int timestamp;
    public LamportMessage(String content, int timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }
}

class LamportProcess extends Thread {
    private final int processId;
    private final AtomicInteger lamportClock = new AtomicInteger(0);
    private final BlockingQueue<LamportMessage>[] channels;
    private final Random random = new Random();

    public LamportProcess(int processId, BlockingQueue<LamportMessage>[] channels) {
        this.processId = processId;
        this.channels = channels;
    }

    private void log(String event) {
        System.out.printf("Processo %d | Relógio de Lamport: %d | Evento: %s\n",
                processId, lamportClock.get(), event);
    }

    private void internalEvent() {
        lamportClock.incrementAndGet();
        log("Executando evento interno.");
    }
    
    private void sendMessage(int recipientId) throws InterruptedException {
        int timestamp = lamportClock.incrementAndGet();
        LamportMessage msg = new LamportMessage("Olá do processo " + processId, timestamp);
        log("Enviando mensagem para o processo " + recipientId + " com T=" + timestamp);
        channels[recipientId].put(msg);
    }

    private void receiveMessage(LamportMessage msg) {
        int receivedTimestamp = msg.timestamp;
        lamportClock.updateAndGet(currentVal -> Math.max(currentVal, receivedTimestamp) + 1);
        log("Recebeu mensagem: '" + msg.content + "'. T_msg=" + receivedTimestamp + ". Novo T=" + lamportClock.get());
    }

    @Override
    public void run() {
        log("Iniciado.");
        try {
            // Evento inicial
            internalEvent();
            Thread.sleep(random.nextInt(1000));

            while (true) {
                Thread.sleep(random.nextInt(2000) + 500);

                if (random.nextBoolean()) {
                    int recipientId = random.nextInt(channels.length);
                    if (recipientId != this.processId) {
                       sendMessage(recipientId);
                    } else {
                        internalEvent();
                    }
                } else {
                    LamportMessage msg = channels[this.processId].poll();
                    if (msg != null) {
                        receiveMessage(msg);
                    } else {
                        internalEvent();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


public class parte2 {
    public static void main(String[] args) {
        int numProcesses = 3;
        @SuppressWarnings("unchecked")
        BlockingQueue<LamportMessage>[] channels = new LinkedBlockingQueue[numProcesses];
        for (int i = 0; i < numProcesses; i++) {
            channels[i] = new LinkedBlockingQueue<>();
        }

        for (int i = 0; i < numProcesses; i++) {
            new LamportProcess(i, channels).start();
        }
    }
}