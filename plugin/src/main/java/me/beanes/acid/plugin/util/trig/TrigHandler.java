package me.beanes.acid.plugin.util.trig;

public enum TrigHandler {
    VANILLA_MATH {
        @Override
        public float sin(float radians) {
            return VanillaMath.sin(radians);
        }

        @Override
        public float cos(float radians) {
            return VanillaMath.cos(radians);
        }
    },
    FAST_MATH {
        @Override
        public float sin(float radians) {
            return FastMath.sin(radians);
        }

        @Override
        public float cos(float radians) {
            return FastMath.cos(radians);
        }
    },
    LEGACY_FAST_MATH {
        @Override
        public float sin(float radians) {
            return LegacyFastMath.sin(radians);
        }

        @Override
        public float cos(float radians) {
            return LegacyFastMath.cos(radians);
        }
    };

    public abstract float sin(float radians);
    public abstract float cos(float radians);
}