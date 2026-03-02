package app.model;


public class Context {
    private int age;
    private boolean fraser;
    private boolean symptomatic;
    private boolean assault;
    private boolean pep;
    private boolean preg;
    private boolean immuno;
    private boolean safeguarding;
    private boolean windowElapsed;
    private Integer recentKitWeeks;
    private boolean understands;
    private boolean clinOverride;

    public Context() {
    }

    private Context(Builder b) {
        this.age = b.age;
        this.fraser = b.fraser;
        this.symptomatic = b.symptomatic;
        this.assault = b.assault;
        this.pep = b.pep;
        this.preg = b.preg;
        this.immuno = b.immuno;
        this.safeguarding = b.safeguarding;
        this.windowElapsed = b.windowElapsed;
        this.recentKitWeeks = b.recentKitWeeks;
        this.understands = b.understands;
        this.clinOverride = b.clinOverride;
    }

    // Getters / setters (shortened names)
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isFraser() {
        return fraser;
    }

    public void setFraser(boolean fraser) {
        this.fraser = fraser;
    }

    public boolean isSymptomatic() {
        return symptomatic;
    }

    public void setSymptomatic(boolean symptomatic) {
        this.symptomatic = symptomatic;
    }

    public boolean isAssault() {
        return assault;
    }

    public void setAssault(boolean assault) {
        this.assault = assault;
    }

    public boolean isPep() {
        return pep;
    }

    public void setPep(boolean pep) {
        this.pep = pep;
    }

    public boolean isPreg() {
        return preg;
    }

    public void setPreg(boolean preg) {
        this.preg = preg;
    }

    public boolean isImmuno() {
        return immuno;
    }

    public void setImmuno(boolean immuno) {
        this.immuno = immuno;
    }

    public boolean hasSafeguarding() {
        return safeguarding;
    }

    public void setSafeguarding(boolean safeguarding) {
        this.safeguarding = safeguarding;
    }

    public boolean isWindowElapsed() {
        return windowElapsed;
    }

    public void setWindowElapsed(boolean windowElapsed) {
        this.windowElapsed = windowElapsed;
    }

    public Integer recentKitWeeks() {
        return recentKitWeeks;
    }

    public void setRecentKitWeeks(Integer recentKitWeeks) {
        this.recentKitWeeks = recentKitWeeks;
    }

    public boolean understands() {
        return understands;
    }

    public void setUnderstands(boolean understands) {
        this.understands = understands;
    }

    public boolean isClinOverride() {
        return clinOverride;
    }

    public void setClinOverride(boolean clinOverride) {
        this.clinOverride = clinOverride;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int age;
        private boolean fraser;
        private boolean symptomatic;
        private boolean assault;
        private boolean pep;
        private boolean preg;
        private boolean immuno;
        private boolean safeguarding;
        private boolean windowElapsed;
        private Integer recentKitWeeks;
        private boolean understands;
        private boolean clinOverride;

        private Builder() {
        }

        public Builder age(int val) {
            this.age = val;
            return this;
        }

        public Builder fraser(boolean val) {
            this.fraser = val;
            return this;
        }

        public Builder symptomatic(boolean val) {
            this.symptomatic = val;
            return this;
        }

        public Builder assault(boolean val) {
            this.assault = val;
            return this;
        }

        public Builder pep(boolean val) {
            this.pep = val;
            return this;
        }

        public Builder preg(boolean val) {
            this.preg = val;
            return this;
        }

        public Builder immuno(boolean val) {
            this.immuno = val;
            return this;
        }

        public Builder safeguarding(boolean val) {
            this.safeguarding = val;
            return this;
        }

        public Builder windowElapsed(boolean val) {
            this.windowElapsed = val;
            return this;
        }

        public Builder recentKitWeeks(Integer val) {
            this.recentKitWeeks = val;
            return this;
        }

        public Builder understands(boolean val) {
            this.understands = val;
            return this;
        }

        public Builder clinOverride(boolean val) {
            this.clinOverride = val;
            return this;
        }

        public Context build() {
            return new Context(this);
        }
    }
}
