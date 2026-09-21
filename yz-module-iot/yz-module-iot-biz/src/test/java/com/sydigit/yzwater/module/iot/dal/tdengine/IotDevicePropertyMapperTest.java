package com.sydigit.yzwater.module.iot.dal.tdengine;

import com.sydigit.yzwater.module.iot.framework.tdengine.core.TDengineTableField;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link IotDevicePropertyMapper} 的单元测试
 */
class IotDevicePropertyMapperTest {

    @Test
    void testAlterProductPropertySTable_shouldNotAddFieldWhenOnlyCaseDiffers() {
        IotDevicePropertyMapper mapper = mock(IotDevicePropertyMapper.class, Answers.CALLS_REAL_METHODS);
        List<TDengineTableField> oldFields = new ArrayList<>(List.of(
                new TDengineTableField("zmqbj_rise", TDengineTableField.TYPE_TINYINT)));
        List<TDengineTableField> newFields = new ArrayList<>(List.of(
                new TDengineTableField("ZMQBJ_rise", TDengineTableField.TYPE_TINYINT)));

        mapper.alterProductPropertySTable(1L, oldFields, newFields);

        verify(mapper, never()).alterProductPropertySTableAddField(anyLong(), any());
        verify(mapper, never()).alterProductPropertySTableDropField(anyLong(), any());
        verify(mapper, never()).alterProductPropertySTableModifyField(anyLong(), any());
    }

}
