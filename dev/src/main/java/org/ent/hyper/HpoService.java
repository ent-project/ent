package org.ent.hyper;

import java.util.Map;

public class HpoService {
    public static class SuggestResponse {
        public int trial_number;
        public Map<String, Object> parameters;

        public int getTrial_number() {
            return trial_number;
        }

        public void setTrial_number(int trial_number) {
            this.trial_number = trial_number;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }

    public static class CompleteRequest {
        private int trial_number;

        private double value;

        public CompleteRequest(int trial_number, double value) {
            this.trial_number = trial_number;
            this.value = value;
        }

        public int getTrial_number() {
            return trial_number;
        }

        public void setTrial_number(int trial_number) {
            this.trial_number = trial_number;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

}
