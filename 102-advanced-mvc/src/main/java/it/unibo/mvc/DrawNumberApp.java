package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private int minimum = MIN;
    private int maximum = MAX;
    private int attempts = ATTEMPTS;
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;
    private static final String ROOT = "config.yml";
    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        try {
            configRead();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            this.model = new DrawNumberImpl(minimum, maximum, attempts);
        }
        
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    private void configRead() throws IOException{
        final InputStream in = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(ROOT));
        try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while((line = br.readLine()) != null) {
                StringTokenizer splitted = new StringTokenizer(line, ": ");
                switch(splitted.nextToken()) {
                    case "minimum" : 
                        this.minimum = Integer.valueOf(splitted.nextToken());
                        break;
                    case "maximum" :
                        this.maximum = Integer.valueOf(splitted.nextToken());
                        break;
                    case "attempts" :
                        this.attempts = Integer.valueOf(splitted.nextToken());
                        break;
                }
            }
        }
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), new PrintStreamView("output.txt"), new PrintStreamView(System.out));
    }

}
