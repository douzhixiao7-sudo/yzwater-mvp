package com.sydigit.yzwater.framework.excel.core.util;

import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.converters.longconverter.LongStringConverter;
import com.sydigit.yzwater.framework.common.util.http.HttpUtils;
import com.sydigit.yzwater.framework.excel.core.handler.ColumnWidthMatchStyleStrategy;
import com.sydigit.yzwater.framework.excel.core.handler.SelectSheetWriteHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Excel 工具类
 *
 *
 */
public class ExcelUtils {

    /**
     * 将列表以 Excel 响应给前端
     *
     * @param response  响应
     * @param filename  文件名
     * @param sheetName Excel sheet 名
     * @param head      Excel head 头
     * @param data      数据列表哦
     * @param <T>       泛型，保证 head 和 data 类型的一致性
     * @throws IOException 写入失败的情况
     */
    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data) throws IOException {
        // 输出 Excel
        FastExcelFactory.write(response.getOutputStream(), head)
                .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                .registerWriteHandler(new SelectSheetWriteHandler(head)) // 基于固定 sheet 实现下拉框
                .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                .sheet(sheetName).doWrite(data);
        // 设置 header 和 contentType。写在最后的原因是，避免报错时，响应 contentType 已经被修改了
        response.addHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8(filename));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    }

    /**
     * 使用显式表头与行数据写出 Excel（不依赖 Java Bean 的 @ExcelProperty，表头与列一一对应即可）
     */
    public static void writeWithHeadList(HttpServletResponse response, String filename, String sheetName,
                                         List<List<String>> head, List<List<Object>> data) throws IOException {
        FastExcelFactory.write(response.getOutputStream())
                .head(head)
                .autoCloseStream(false)
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy())
                .registerConverter(new LongStringConverter())
                .sheet(sheetName)
                .doWrite(data);
        response.addHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8(filename));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    }

    public static <T> List<T> read(MultipartFile file, Class<T> head) throws IOException {
        return FastExcelFactory.read(file.getInputStream(), head, null)
                .autoCloseStream(false)  // 不要自动关闭，交给 Servlet 自己处理
                .doReadAllSync();
    }

    /**
     * 仅读取指定 sheet 的内容（sheetNo 从 0 开始）
     *
     * <p>用于导入场景：模板可能包含多个 sheet，但业务只需要读取第一个 sheet。</p>
     */
    public static <T> List<T> readSheet(MultipartFile file, Class<T> head, int sheetNo) throws IOException {
        return FastExcelFactory.read(file.getInputStream(), head, null)
                .autoCloseStream(false)  // 不要自动关闭，交给 Servlet 自己处理
                .sheet(sheetNo)
                .doReadSync();
    }

}
