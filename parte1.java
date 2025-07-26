import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Classe que representa a mensagem trocada entre processos
class Message {
    public final String content;
    public Message(String content) {
        this.content = content;
    }
}

// Classe que representa um processo
class Process extends Thread {
    private final int processId;
    private final BlockingQueue<Message>[] channels;
    private final Random random = new Random();

    public Process(int processId, BlockingQueue<Message>[] channels) {
        this.processId = processId;
        this.channels = channels;
    }

    private void log(String event) {
        System.out.printf("Processo %d | Relógio Físico: %d | Evento: %s\n",
                processId, System.currentTimeMillis(), event);
    }

    @Override
    public void run() {
        log("Iniciado.");
        try {
            while (true) {
                // Simula um evento interno
                Thread.sleep(random.nextInt(2000) + 500);
                log("Executando evento interno.");

                // Decide se envia ou tenta receber uma mensagem
                if (random.nextBoolean()) {
                    int recipientId = random.nextInt(channels.length);
                    if (recipientId != this.processId) {
                        Message msg = new Message("Olá do processo " + this.processId);
                        log("Enviando mensagem para o processo " + recipientId);
                        channels[recipientId].put(msg);
                    }
                } else {
                    Message receivedMsg = channels[this.processId].poll();
                    if (receivedMsg != null) {
                        log("Recebeu mensagem: '" + receivedMsg.content + "'");
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class parte1 {
    public static void main(String[] args) {
        int numProcesses = 3;
        @SuppressWarnings("unchecked")
        BlockingQueue<Message>[] channels = new LinkedBlockingQueue[numProcesses];
        for (int i = 0; i < numProcesses; i++) {
            channels[i] = new LinkedBlockingQueue<>();
        }

        for (int i = 0; i < numProcesses; i++) {
            new Process(i, channels).start();
        }
    }
}