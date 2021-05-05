package io.bidmachine.test_utils;

import android.view.View;
import android.widget.TextView;

public abstract class Condition {

    public abstract boolean check(View view);

    public static Condition byText(String text) {
        return new ByText(text);
    }

    public static Condition byClassName(String className) {
        return new ByClassName(className);
    }

    static class ByText extends Condition {

        private final String text;

        ByText(String text) {
            this.text = text;
        }

        @Override
        public boolean check(View view) {
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                return textView.getText().equals(text);
            }

            return false;
        }
    }

    static class ByClassName extends Condition {

        private final String className;

        ByClassName(String className) {
            this.className = className;
        }

        @Override
        public boolean check(View view) {
            return view.getClass().getName().equals(className);
        }
    }

}