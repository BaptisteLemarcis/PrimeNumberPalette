package ca.uqac.liara;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SingleProcessor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by baptiste on 2/6/2017.
 */
public class PrimeNumberFinderProcessor extends SingleProcessor {

    private final int NBTHREAD = 64;
    private AtomicInteger nbMod = new AtomicInteger(0);

    public PrimeNumberFinderProcessor() {
        super(1, 1);
    }

    @Override
    protected Queue<Object[]> compute(Object[] objects) {
        BigInteger number;
        try {
            number = (BigInteger)objects[0];
        } catch (Exception e){
            number = new BigInteger((String)objects[0]);
        }

        BigInteger sqrtNumber = sqrt(number);

        //ThreadGroup thdGrp = new ThreadGroup("SqrtGroup");
        ArrayList<Thread> threads = new ArrayList<Thread>();

        //System.out.println("SQRT = " + sqrtNumber);

        for(BigInteger i = BigInteger.valueOf(2); i.compareTo(sqrtNumber) >= 0; i=i.add(BigInteger.valueOf(NBTHREAD))){

            for(int j = 0; j < NBTHREAD; j++){
                threads.add(new Thread(new SqrtProcess(number, i.add(BigInteger.valueOf(j)))));
            }

            for(int j = 0; j < NBTHREAD; j++){
                threads.get(j).start();
            }

            for(int j = 0; j < NBTHREAD; j++){
                try {
                    threads.get(j).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(nbMod.intValue() == -1) return wrapObject(BigInteger.valueOf(-1));

            threads.clear();
            //if (number.mod(i).equals(BigInteger.ZERO)) return wrapObject(BigInteger.valueOf(-1));
        }

        /*for(BigInteger i = BigInteger.valueOf(2); !i.equals(sqrtNumber); i=i.add(BigInteger.ONE)){
            if (number.mod(i).equals(BigInteger.ZERO)) return wrapObject(BigInteger.valueOf(-1));
        }*/
        return wrapObject(number);
    }

    @Override
    public Processor clone() {
        return null;
    }

    private BigInteger sqrt(BigInteger n) {
        BigInteger a = BigInteger.ONE;
        BigInteger b = n.shiftRight(5).add(BigInteger.valueOf(8));
        while (b.compareTo(a) >= 0) {
            BigInteger mid = a.add(b).shiftRight(1);
            if (mid.multiply(mid).compareTo(n) > 0) {
                b = mid.subtract(BigInteger.ONE);
            } else {
                a = mid.add(BigInteger.ONE);
            }
        }
        return a.subtract(BigInteger.ONE);
    }

    class SqrtProcess implements Runnable {
        BigInteger m_nb;
        BigInteger m_mod;

        SqrtProcess(BigInteger nb, BigInteger mod){
            m_nb = nb;
            m_mod = mod;
        }

        public void run() {
            BigInteger res = m_nb.mod(m_mod);
            //System.out.println(m_nb + " mod " + m_mod + " = " + res);
            if (res.equals(BigInteger.ZERO)){
                nbMod.set(-1);
            }
        }

    }
}
