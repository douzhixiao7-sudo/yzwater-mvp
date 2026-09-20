package com.sydigit.yzwater.module.constants;

/**
 * 统一引用对象类型常量。
 *
 * <p>用于河长表、公示牌表等业务表中 reference_type 字段取值。</p>
 */
public final class ReferenceTypeConstants {

    /**
     * 河道
     */
    public static final String RIVER = "river";

    /**
     * 河段
     */
    public static final String RIVER_SECTION = "river_section";

    /**
     * 水库
     */
    public static final String RESERVOIR = "reservoir";

    /**
     * 大屏-BF 河道
     */
    public static final String RIVER_BF = "river_bf";

    /**
     * 大屏-BF 河段
     */
    public static final String RIVER_SECTION_BF = "river_section_bf";

    /**
     * 大屏-BF 水库
     */
    public static final String RESERVOIR_BF = "reservoir_bf";

    /**
     * 总河长
     */
    public static final String TOTAL_CHIEF = "total_chief";

    private ReferenceTypeConstants() {
    }
}
