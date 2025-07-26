import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class VectorMessage {
    public final String content;
    public final int[] vectorClock;

    public VectorMessage(String content, int[] vectorClock) {
        this.content = content;
        this.vectorClock = Arrays.copyOf(vectorClock, vectorClock.length);
    }
}

class VectorProcess extends Thread {
    private final int processId;
    private final int numProcesses;
    private final int[] vectorClock;
    private final BlockingQueue<VectorMessage>[] channels;
    private final Random random = new Random();

    public VectorProcess(int processId, int numProcesses, BlockingQueue<VectorMessage>[] channels) {
        this.processId = processId;
        this.numProcesses = numProcesses;
        this.channels = channels;
        this.vectorClock = new int[numProcesses]; // Inicializado com zeros [cite: 463]
    }

    private void log(String event) {
        System.out.printf("Processo %d | Relógio Vetorial: %s | Evento: %s\n",
                processId, Arrays.toString(vectorClock), event);
    }

    private synchronized void localEvent() {
        vectorClock[processId]++;
        log("Executando evento interno.");
    }
    
    private synchronized void sendMessage(int recipientId) throws InterruptedException {
        vectorClock[processId]++;
        log("Enviando mensagem para o processo " + recipientId);
        VectorMessage msg = new VectorMessage("Olá de " + processId, vectorClock);
        channels[recipientId].put(msg);
    }

    private synchronized void receiveMessage(VectorMessage msg) {
        int[] receivedVector = msg.vectorClock;
        // Regra de recepção (RV4) 
        for (int i = 0; i < numProcesses; i++) {
            vectorClock[i] = Math.max(vectorClock[i], receivedVector[i]);
        }
        vectorClock[processId]++; // Incremento para o evento de recebimento
        log("Recebeu mensagem de '" + msg.content + "'. Vetor recebido: " + Arrays.toString(receivedVector));
    }

    @Override
    public void run() {
        log("Iniciado.");
        try {
            while (true) {
                Thread.sleep(random.nextInt(3000) + 1000);

                if (random.nextBoolean()) {
                     int recipientId = random.nextInt(numProcesses);
                    if (recipientId != this.processId) {
                       sendMessage(recipientId);
                    } else {
                        localEvent();
                    }
                } else {
                    VectorMessage msg = channels[this.processId].poll();
                    if (msg != null) {
                        receiveMessage(msg);
                    } else {
                       localEvent();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class parte3 {
    public static void main(String[] args) {
        int numProcesses = 3;
        @SuppressWarnings("unchecked")
        BlockingQueue<VectorMessage>[] channels = new LinkedBlockingQueue[numProcesses];
        for (int i = 0; i < numProcesses; i++) {
            channels[i] = new LinkedBlockingQueue<>();
        }

        for (int i = 0; i < numProcesses; i++) {
            new VectorProcess(i, numProcesses, channels).start();
        }
    }
}