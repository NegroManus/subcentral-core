package de.subcentral.core.parse;

import java.util.Locale;
import java.util.function.UnaryOperator;

import de.subcentral.core.util.TimeUtil;

public class LambdaPerfTest {
    public static final UnaryOperator<String> STATIC_OP = new UnaryOperator<String>() {
        @Override
        public String apply(String t) {
            return t.toUpperCase(Locale.getDefault()).toLowerCase(Locale.getDefault()).toUpperCase(Locale.getDefault()).replaceAll("\\w", "!");
        }
    };

    public static void main(String[] args) throws Exception {
        String[] strings = new String[] { "Boardwalk.Empire.S04E01.MULTi.1080p.BluRay.x264-SODAPOP", "iCarly.S06E06.iBattle.Chip.720p.HDTV.x264-W4F" };

        long start = System.nanoTime();
        for (int i = 0; i < 1_000; i++) {
            for (String s : strings) {
                String newStr = doItWithLambdaCapturing(s);
                System.out.println(newStr);
            }
        }

        System.out.println(TimeUtil.durationMillisDouble(start));
    }

    // 80
    // 120
    // 800
    private static String doItOldFashioned(String string) {
        return string.toUpperCase(Locale.getDefault()).toLowerCase(Locale.getDefault()).toUpperCase(Locale.getDefault()).replaceAll("\\w", "!");
    }

    // 120
    // 150
    // 1000
    private static String doItWithLambda(String string) {
        UnaryOperator<String> op = s -> s.toUpperCase(Locale.getDefault()).toLowerCase(Locale.getDefault()).toUpperCase(Locale.getDefault()).replaceAll("\\w", "!");
        return op.apply(string);
    }

    // 120
    // 160
    // 1200
    private static String doItWithLambdaStatic(String string) {
        UnaryOperator<String> op = (LambdaPerfTest::doItOldFashioned);
        return op.apply(string);
    }

    //
    // 115
    // 850
    private static String doItWithLambdaCapturing(String string) {
        String replacement = "!";
        UnaryOperator<String> op = s -> s.toUpperCase(Locale.getDefault()).toLowerCase(Locale.getDefault()).toUpperCase(Locale.getDefault()).replaceAll("\\w", replacement);
        return op.apply(string);
    }

    // 85
    // 120
    // 840
    private static String doItWithAnonymous(String string) {
        UnaryOperator<String> op = new UnaryOperator<String>() {
            @Override
            public String apply(String t) {
                return t.toUpperCase(Locale.getDefault()).toLowerCase(Locale.getDefault()).toUpperCase(Locale.getDefault()).replaceAll("\\w", "!");
            }
        };
        return op.apply(string);
    }

    // 80
    // 110
    // 840
    private static String doItWithClass(String string) {
        return STATIC_OP.apply(string);
    }
}
