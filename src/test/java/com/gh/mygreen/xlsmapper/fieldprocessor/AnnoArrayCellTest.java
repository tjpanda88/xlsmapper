package com.gh.mygreen.xlsmapper.fieldprocessor;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import static com.gh.mygreen.xlsmapper.TestUtils.*;
import static com.gh.mygreen.xlsmapper.xml.XmlBuilder.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gh.mygreen.xlsmapper.AnnotationInvalidException;
import com.gh.mygreen.xlsmapper.XlsMapper;
import com.gh.mygreen.xlsmapper.annotation.ArrayDirection;
import com.gh.mygreen.xlsmapper.annotation.XlsArrayCell;
import com.gh.mygreen.xlsmapper.annotation.XlsArrayOperator;
import com.gh.mygreen.xlsmapper.annotation.XlsBooleanConverter;
import com.gh.mygreen.xlsmapper.annotation.XlsDateConverter;
import com.gh.mygreen.xlsmapper.annotation.XlsDefaultValue;
import com.gh.mygreen.xlsmapper.annotation.XlsFormula;
import com.gh.mygreen.xlsmapper.annotation.XlsNumberConverter;
import com.gh.mygreen.xlsmapper.annotation.XlsSheet;
import com.gh.mygreen.xlsmapper.annotation.XlsTrim;
import com.gh.mygreen.xlsmapper.annotation.XlsArrayOperator.OverOperate;
import com.gh.mygreen.xlsmapper.annotation.XlsArrayOperator.RemainedOperate;
import com.gh.mygreen.xlsmapper.fieldprocessor.impl.ArrayCellProcessor;
import com.gh.mygreen.xlsmapper.util.CellPosition;
import com.gh.mygreen.xlsmapper.validation.SheetBindingErrors;
import com.gh.mygreen.xlsmapper.xml.bind.XmlInfo;

/**
 * {@link ArrayCellProcessor}のテスタ。
 * アノテーション{@link XlsArrayCell}のテスタ。
 *
 * @since 2.0
 * @author T.TSUCHIE
 *
 */
public class AnnoArrayCellTest {
    
    /**
     * テスト結果ファイルの出力ディレクトリ
     */
    private static File OUT_DIR;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        OUT_DIR = createOutDir();
    }
    
    /**
     * 読み込みのテスト - 通常のデータ
     * <p>クラスタイプ、座標の指定方法の確認</p>
     */
    @Test
    public void test_load_array_cell_normal() throws Exception {
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_ArrayCell.xlsx")) {
            SheetBindingErrors<NormalSheet> errors = mapper.loadDetail(in, NormalSheet.class);
            
            NormalSheet sheet = errors.getTarget();
            
            assertThat(sheet.labels).isNull();
            
            assertThat(sheet.horizontal1)
                .hasSize(5)
                .containsExactly("あ", "い", "う", "え", "お");
            
            // 位置情報の比較
            assertThat(sheet.positions)
                .containsEntry("horizontal1[0]", CellPosition.of("B4"))
                .containsEntry("horizontal1[1]", CellPosition.of("C4"))
                .containsEntry("horizontal1[2]", CellPosition.of("D4"))
                .containsEntry("horizontal1[3]", CellPosition.of("E4"))
                .containsEntry("horizontal1[4]", CellPosition.of("F4"))
                ;
            
            assertThat(sheet.vertical1)
                .hasSize(3)
                .containsExactly(LocalDate.of(2017, 4, 1), LocalDate.of(2017, 4, 2), LocalDate.of(2017, 4, 3));
            
            // 位置情報の比較
            assertThat(sheet.positions)
                .containsEntry("vertical1[0]", CellPosition.of("B6"))
                .containsEntry("vertical1[1]", CellPosition.of("B7"))
                .containsEntry("vertical1[2]", CellPosition.of("B8"))
                ;
        }
    }
    
    /**
     * 読み込みのテスト - 不正なアノテーション - サポートしていないタイプの場合
     */
    @Test
    public void test_load_array_cell_invalidAnno_notSupportType() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(false);
        
        // アノテーションの変更 - タイプが不正
        XmlInfo xmlInfo = createXml()
                .classInfo(createClass(InvalidAnnoSheet.class)
                        .field(createField("field2")
                                .annotation(createAnnotation(XlsArrayCell.class)
                                        .attribute("address", "B4")
                                        .attribute("size", 3)
                                        .buildAnnotation())
                                .buildField())
                    .buildClass())
                .buildXml();
        mapper.getConiguration().setAnnotationMapping(xmlInfo);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_ArrayCell.xlsx")) {
            
            assertThatThrownBy(() -> mapper.load(in, InvalidAnnoSheet.class))
                .isInstanceOf(AnnotationInvalidException.class)
                .hasMessage("'com.gh.mygreen.xlsmapper.fieldprocessor.AnnoArrayCellTest$InvalidAnnoSheet#field2' において、アノテーション @XlsArrayCell を付与したタイプ 'java.lang.String' はサポートしていません。'Collection(List/Set) or Array' で設定してください。");
        }
        
    }
    
    /**
     * 読み込みのテスト - 不正なアノテーション - アドレスの指定が不正
     */
    @Test
    public void test_load_array_cell_invalidAnno_wrongAddress() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(false);
        
        {
            // アノテーションの変更 - addressが不正
            XmlInfo xmlInfo = createXml()
                    .classInfo(createClass(InvalidAnnoSheet.class)
                            .field(createField("field1")
                                    .annotation(createAnnotation(XlsArrayCell.class)
                                            .attribute("address", "あいう")
                                            .buildAnnotation())
                                    .buildField())
                        .buildClass())
                    .buildXml();
            mapper.getConiguration().setAnnotationMapping(xmlInfo);
            
            try(InputStream in = new FileInputStream("src/test/data/anno_ArrayCell.xlsx")) {
                
                assertThatThrownBy(() -> mapper.load(in, InvalidAnnoSheet.class))
                    .isInstanceOf(AnnotationInvalidException.class)
                    .hasMessage("'com.gh.mygreen.xlsmapper.fieldprocessor.AnnoArrayCellTest$InvalidAnnoSheet#field1' において、アノテーション @XlsArrayCell の属性 'address' の値（あいう）は、セルのアドレスの書式として不正です。");
            }
        
        }
        
        {
            // アノテーションの変更 - rowが不正
            XmlInfo xmlInfo = createXml()
                    .classInfo(createClass(InvalidAnnoSheet.class)
                            .field(createField("field1")
                                    .annotation(createAnnotation(XlsArrayCell.class)
                                            .attribute("address", "")
                                            .attribute("row", -1)
                                            .attribute("column", 3)
                                            .buildAnnotation())
                                    .buildField())
                        .buildClass())
                    .buildXml();
            mapper.getConiguration().setAnnotationMapping(xmlInfo);
            
            try(InputStream in = new FileInputStream("src/test/data/anno_ArrayCell.xlsx")) {
                
                assertThatThrownBy(() -> mapper.load(in, InvalidAnnoSheet.class))
                    .isInstanceOf(AnnotationInvalidException.class)
                    .hasMessage("'com.gh.mygreen.xlsmapper.fieldprocessor.AnnoArrayCellTest$InvalidAnnoSheet#field1' において、アノテーション @XlsArrayCell の属性 'row' の値（-1）は、0以上の値を設定してください。");
            }
        
        }
        
        {
            // アノテーションの変更 - columnが不正
            XmlInfo xmlInfo = createXml()
                    .classInfo(createClass(InvalidAnnoSheet.class)
                            .field(createField("field1")
                                    .annotation(createAnnotation(XlsArrayCell.class)
                                            .attribute("address", "")
                                            .attribute("row", 2)
                                            .attribute("column", -1)
                                            .buildAnnotation())
                                    .buildField())
                        .buildClass())
                    .buildXml();
            mapper.getConiguration().setAnnotationMapping(xmlInfo);
            
            try(InputStream in = new FileInputStream("src/test/data/anno_ArrayCell.xlsx")) {
                
                assertThatThrownBy(() -> mapper.load(in, InvalidAnnoSheet.class))
                    .isInstanceOf(AnnotationInvalidException.class)
                    .hasMessage("'com.gh.mygreen.xlsmapper.fieldprocessor.AnnoArrayCellTest$InvalidAnnoSheet#field1' において、アノテーション @XlsArrayCell の属性 'column' の値（-1）は、0以上の値を設定してください。");
            }
        
        }
        
    }
    
    /**
     * 読み込みのテスト - 不正なアノテーション - sizeの指定が不正
     */
    @Test
    public void test_load_array_cell_invalidAnno_wrongSize() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(false);
        
        // アノテーションの変更 - sizeが不正
        XmlInfo xmlInfo = createXml()
                .classInfo(createClass(InvalidAnnoSheet.class)
                        .field(createField("field1")
                                .annotation(createAnnotation(XlsArrayCell.class)
                                        .attribute("address", "B4")
                                        .attribute("size", 0)
                                        .buildAnnotation())
                                .buildField())
                    .buildClass())
                .buildXml();
        mapper.getConiguration().setAnnotationMapping(xmlInfo);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_ArrayCell.xlsx")) {
            
            assertThatThrownBy(() -> mapper.load(in, InvalidAnnoSheet.class))
                .isInstanceOf(AnnotationInvalidException.class)
                .hasMessage("'com.gh.mygreen.xlsmapper.fieldprocessor.AnnoArrayCellTest$InvalidAnnoSheet#field1' において、アノテーション @XlsArrayCell の属性 'size' の値（0）は、1以上の値を設定してください。");
        }
        
    }
    
    /**
     * 読み込みのテスト - 結合の考慮
     */
    @Test
    public void test_load_array_cell_itemMerged() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_ArrayCell.xlsx")) {
            SheetBindingErrors<ItemMergedSheet> errors = mapper.loadDetail(in, ItemMergedSheet.class);
            
            ItemMergedSheet sheet = errors.getTarget();
            
            assertThat(sheet.labels).isNull();
            
            // horizontal - 結合を考慮しない
            assertThat(sheet.hMergedFalse)
                .hasSize(5)
                .containsExactly("今日は", "今日は", "、", "いい天気ですね。", "いい天気ですね。");
            
            assertThat(sheet.positions)
                .containsEntry("hMergedFalse[0]", CellPosition.of("B4"))
                .containsEntry("hMergedFalse[1]", CellPosition.of("C4"))
                .containsEntry("hMergedFalse[2]", CellPosition.of("D4"))
                .containsEntry("hMergedFalse[3]", CellPosition.of("E4"))
                .containsEntry("hMergedFalse[4]", CellPosition.of("F4"))
                ;
            
            // horizontal - 結合を考慮する
            assertThat(sheet.hMergedTrue)
                .hasSize(3)
                .containsExactly("今日は", "、", "いい天気ですね。");
            
            assertThat(sheet.positions)
                .containsEntry("hMergedTrue[0]", CellPosition.of("B5"))
                .containsEntry("hMergedTrue[1]", CellPosition.of("D5"))
                .containsEntry("hMergedTrue[2]", CellPosition.of("E5"))
                ;
            
            // vertical - 結合を考慮しない
            assertThat(sheet.vMergedFalse)
                .hasSize(5)
                .containsExactly("Hello", "Hello", ",", "World", "World");
            
            assertThat(sheet.positions)
                .containsEntry("vMergedFalse[0]", CellPosition.of("C7"))
                .containsEntry("vMergedFalse[1]", CellPosition.of("C8"))
                .containsEntry("vMergedFalse[2]", CellPosition.of("C9"))
                .containsEntry("vMergedFalse[3]", CellPosition.of("C10"))
                .containsEntry("vMergedFalse[4]", CellPosition.of("C11"))
                ;
            
            // vertical - 結合を考慮する
            assertThat(sheet.vMergedTrue)
                .hasSize(3)
                .containsExactly("Hello", ",", "World");
            
            assertThat(sheet.positions)
                .containsEntry("vMergedTrue[0]", CellPosition.of("D7"))
                .containsEntry("vMergedTrue[1]", CellPosition.of("D9"))
                .containsEntry("vMergedTrue[2]", CellPosition.of("D10"))
                ;
            
        }
    }
    
    /**
     * 読み込みのテスト - 型変換のテスト
     */
    @Test
    public void test_load_array_cell_convert() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_ArrayCell.xlsx")) {
            SheetBindingErrors<ConverterSheet> errors = mapper.loadDetail(in, ConverterSheet.class);
            
            ConverterSheet sheet = errors.getTarget();
            
            assertThat(sheet.labels).isNull();
            
            assertThat(sheet.text).containsExactly("あいう", "かきく", "", "");
            
            assertThat(sheet.bool).containsExactly(Boolean.TRUE, Boolean.FALSE, null, null);
            assertThat(cellFieldError(errors, cellAddress(sheet.positions.get("bool[3]"))).isConversionFailure()).isTrue();
            
            assertThat(sheet.number_int).containsExactly(1234, 0, 100, null);
            assertThat(cellFieldError(errors, cellAddress(sheet.positions.get("number_int[3]"))).isConversionFailure()).isTrue();
            
            assertThat(sheet.number_double).containsExactly(1234.567d, 0.0d, 0.0d, 0.0d);
            assertThat(cellFieldError(errors, cellAddress(sheet.positions.get("number_double[3]"))).isConversionFailure()).isTrue();
            
        }
        
    }
    
    /**
     * 読み込みのテスト - 数式
     */
    @Test
    public void test_load_array_cell_formula() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_ArrayCell.xlsx")) {
            SheetBindingErrors<FormulaSheet> errors = mapper.loadDetail(in, FormulaSheet.class);
            
            FormulaSheet sheet = errors.getTarget();
            
            assertThat(sheet.labels).isNull();
            
            assertThat(sheet.continueNumber)
                .containsExactly(1, 2, 3);
            
            assertThat(sheet.dateList)
                .containsExactly(LocalDate.of(2017, 5, 5), LocalDate.of(2017, 5, 6), LocalDate.of(2017, 5, 7));
            
        }
    }
    
    /**
     * 書き込みのテスト - 通常のデータ
     */
    @Test
    public void test_save_array_cell_normal() throws Exception {
        
        // テストデータの作成
        final NormalSheet outSheet = new NormalSheet();
        
        outSheet.horizontal1 = Arrays.asList("あ", "い", "う", "え", "お");
        outSheet.vertical1 = new LocalDate[]{LocalDate.of(2017, 4, 1), LocalDate.of(2017, 4, 2), LocalDate.of(2017, 4, 3)};
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            SheetBindingErrors<NormalSheet> errors = mapper.loadDetail(in, NormalSheet.class);
            
            NormalSheet sheet = errors.getTarget();
            
            assertThat(sheet.positions).containsAllEntriesOf(outSheet.positions);
            assertThat(sheet.labels).isNull();
            assertThat(outSheet.labels).isNull();
            
            assertThat(sheet.horizontal1).containsExactlyElementsOf(outSheet.horizontal1);
            assertThat(sheet.vertical1).containsExactly(outSheet.vertical1);
        }
        
    }
    
    /**
     * 書き込みのテスト - 不正なアノテーション - サポートしていないタイプの場合
     */
    @Test
    public void test_save_array_cell_invalidAnno_notSupportType() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(false);
        
        // テストデータの作成
        final InvalidAnnoSheet outSheet = new InvalidAnnoSheet();
        outSheet.field1 = Arrays.asList("あ", "い", "う");
        outSheet.field2 = "あいう";
        
        // アノテーションの変更 - タイプが不正
        XmlInfo xmlInfo = createXml()
                .classInfo(createClass(InvalidAnnoSheet.class)
                        .field(createField("field2")
                                .annotation(createAnnotation(XlsArrayCell.class)
                                        .attribute("address", "B4")
                                        .attribute("size", 3)
                                        .buildAnnotation())
                                .buildField())
                    .buildClass())
                .buildXml();
        mapper.getConiguration().setAnnotationMapping(xmlInfo);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            assertThatThrownBy(() -> mapper.save(template, out, outSheet))
                .isInstanceOf(AnnotationInvalidException.class)
                .hasMessage("'com.gh.mygreen.xlsmapper.fieldprocessor.AnnoArrayCellTest$InvalidAnnoSheet#field2' において、アノテーション @XlsArrayCell を付与したタイプ 'java.lang.String' はサポートしていません。'Collection(List/Set) or Array' で設定してください。");
            
        }
        
    }
    
    /**
     * 書き込みのテスト - 不正なアノテーション - sizeの指定が不正
     */
    @Test
    public void test_save_array_cell_invalidAnno_wrongSize() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(false);
        
        // テストデータの作成
        final InvalidAnnoSheet outSheet = new InvalidAnnoSheet();
        outSheet.field1 = Arrays.asList("あ", "い", "う");
        
        // アノテーションの変更 - sizeが不正
        XmlInfo xmlInfo = createXml()
                .classInfo(createClass(InvalidAnnoSheet.class)
                        .field(createField("field1")
                                .annotation(createAnnotation(XlsArrayCell.class)
                                        .attribute("address", "B4")
                                        .attribute("size", 0)
                                        .buildAnnotation())
                                .buildField())
                    .buildClass())
                .buildXml();
        mapper.getConiguration().setAnnotationMapping(xmlInfo);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            assertThatThrownBy(() -> mapper.save(template, out, outSheet))
                .isInstanceOf(AnnotationInvalidException.class)
                .hasMessage("'com.gh.mygreen.xlsmapper.fieldprocessor.AnnoArrayCellTest$InvalidAnnoSheet#field1' において、アノテーション @XlsArrayCell の属性 'size' の値（0）は、1以上の値を設定してください。");
            
        }
        
    }
    
    /**
     * 書き込みのテスト - 結合の考慮
     */
    @Test
    public void test_save_array_cell_itemMerged() throws Exception {
        
        // テストデータの作成
        ItemMergedSheet outSheet = new ItemMergedSheet();
        
        outSheet.hMergedFalse = Arrays.asList("今日は", "、", "いい天気ですね。", "明日も", "晴れると良いですね。");
        outSheet.hMergedTrue = Arrays.asList("今日は", "、", "いい天気ですね。");
        
        outSheet.vMergedFalse = Arrays.asList("Hello", ",", "World", "Good", "morning");
        outSheet.vMergedTrue = Arrays.asList("Hello", ",", "World");
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            SheetBindingErrors<ItemMergedSheet> errors = mapper.loadDetail(in, ItemMergedSheet.class);
            
            ItemMergedSheet sheet = errors.getTarget();
            
            assertThat(sheet.positions).containsAllEntriesOf(outSheet.positions);
            assertThat(sheet.labels).isNull();
            assertThat(outSheet.labels).isNull();
            
            assertThat(sheet.hMergedFalse).containsExactlyElementsOf(outSheet.hMergedFalse);
            assertThat(sheet.hMergedTrue).containsExactlyElementsOf(outSheet.hMergedTrue);
            
            assertThat(sheet.vMergedFalse).containsExactlyElementsOf(outSheet.vMergedFalse);
            assertThat(sheet.vMergedTrue).containsExactlyElementsOf(outSheet.vMergedTrue);
        }
        
    }
    
    /**
     * 書き込みのテスト - 余ったりした配列の操作
     */
    @Test
    public void test_save_array_cell_arrayOperator_horizon() throws Exception {
        
        // テストデータの作成
        HorizontalArrayOperatorSheet outSheet = new HorizontalArrayOperatorSheet();
        
        outSheet.overBreak = Arrays.asList("あ", "い", "う", "え");
        
        outSheet.remainedNone = Arrays.asList("あ", "い", "う");
        outSheet.remainedClear = Arrays.asList("あ", "い", "う");
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            SheetBindingErrors<HorizontalArrayOperatorSheet> errors = mapper.loadDetail(in, HorizontalArrayOperatorSheet.class);
            
            HorizontalArrayOperatorSheet sheet = errors.getTarget();
            
            assertThat(sheet.positions).containsAllEntriesOf(outSheet.positions);
            assertThat(sheet.labels).isNull();
            assertThat(outSheet.labels).isNull();
            
            assertThat(sheet.overBreak).containsExactly("あ", "い", "う");
            
            assertThat(sheet.remainedNone).containsExactly("あ", "い", "う", "-", "-");
            assertThat(sheet.remainedClear).containsExactly("あ", "い", "う", null, null);
        }
        
    }
    
    /**
     * 書き込みのテスト - 足りないときエラーをスローする場合
     */
    @Test
    public void test_save_array_cell_arrayOperator_horizon_error() throws Exception {
        
        // テストデータの作成
        HorizontalArrayOperatorSheet outSheet = new HorizontalArrayOperatorSheet();
        
        outSheet.overError = Arrays.asList("あ", "い", "う", "え");
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            assertThatThrownBy(() -> mapper.save(template, out, outSheet))
                .isInstanceOf(AnnotationInvalidException.class)
                .hasMessage("'com.gh.mygreen.xlsmapper.fieldprocessor.AnnoArrayCellTest$HorizontalArrayOperatorSheet#overError' において、 @XlsArrayCell の属性 'size' の値（3）は、書き込むデータサイズ（4）超えています。");
            
        }
    }
    
    /**
     * 書き込みのテスト - 余ったりした配列の操作
     */
    @Test
    public void test_save_array_cell_arrayOperator_vertical() throws Exception {
        
        // テストデータの作成
        VerticalArrayOperatorSheet outSheet = new VerticalArrayOperatorSheet();
        
        outSheet.overBreak = Arrays.asList("あ", "い", "う", "え");
        
        outSheet.remainedNone = Arrays.asList("あ", "い", "う");
        outSheet.remainedClear = Arrays.asList("あ", "い", "う");
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            SheetBindingErrors<VerticalArrayOperatorSheet> errors = mapper.loadDetail(in, VerticalArrayOperatorSheet.class);
            
            VerticalArrayOperatorSheet sheet = errors.getTarget();
            
            assertThat(sheet.positions).containsAllEntriesOf(outSheet.positions);
            assertThat(sheet.labels).isNull();
            assertThat(outSheet.labels).isNull();
            
            assertThat(sheet.overBreak).containsExactly("あ", "い", "う");
            
            assertThat(sheet.remainedNone).containsExactly("あ", "い", "う", "-", "-");
            assertThat(sheet.remainedClear).containsExactly("あ", "い", "う", null, null);
        }
        
    }
    
    /**
     * 書き込みのテスト - 足りないときエラーをスローする場合
     */
    @Test
    public void test_save_array_cell_arrayOperator_vertical_error() throws Exception {
        
        // テストデータの作成
        VerticalArrayOperatorSheet outSheet = new VerticalArrayOperatorSheet();
        
        outSheet.overError = Arrays.asList("あ", "い", "う", "え");
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            assertThatThrownBy(() -> mapper.save(template, out, outSheet))
                .isInstanceOf(AnnotationInvalidException.class)
                .hasMessage("'com.gh.mygreen.xlsmapper.fieldprocessor.AnnoArrayCellTest$VerticalArrayOperatorSheet#overError' において、 @XlsArrayCell の属性 'size' の値（3）は、書き込むデータサイズ（4）超えています。");
            
        }
    }
    
    /**
     * 書き込みのテスト - 型変換
     */
    @Test
    public void test_save_array_cell_converter() throws Exception {
        
        // テストデータの作成
        ConverterSheet outSheet = new ConverterSheet();
        
        outSheet.text = Arrays.asList("あいう", "  かきく  ", "", null);
        outSheet.bool = Arrays.asList(Boolean.TRUE, Boolean.FALSE, null, null);
        outSheet.number_int = Arrays.asList(1234, 0, null, null);
        outSheet.number_double = new double[]{1234.567d, 0.0d, 0.0d, 0.0d};
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            SheetBindingErrors<ConverterSheet> errors = mapper.loadDetail(in, ConverterSheet.class);
            
            ConverterSheet sheet = errors.getTarget();
            
            assertThat(sheet.positions).containsAllEntriesOf(outSheet.positions);
            assertThat(sheet.labels).isNull();
            assertThat(outSheet.labels).isNull();
            
            assertThat(sheet.text).containsExactly("あいう", "かきく", "", "");
            
            assertThat(sheet.bool).containsExactly(Boolean.TRUE, Boolean.FALSE, null, null);
            
            assertThat(sheet.number_int).containsExactly(1234, 0, 100, 100);
            
            assertThat(sheet.number_double).containsExactly(1234.567d, 0.0d, 0.0d, 0.0d);
            
        }
        
    }
    
    /**
     * 書き込みのテスト - 数式のテスト
     */
    @Test
    public void test_save_array_cell_formula() throws Exception {
        
        // テストデータの作成
        FormulaSheet outSheet = new FormulaSheet();
        
        outSheet.continueNumber = new int[3];
        outSheet.dateList = Arrays.asList(null, null, null);
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConiguration().setContinueTypeBindFailure(true);
        
        File outFile = new File(OUT_DIR, "anno_ArrayCell_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_ArrayCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            SheetBindingErrors<FormulaSheet> errors = mapper.loadDetail(in, FormulaSheet.class);
            
            FormulaSheet sheet = errors.getTarget();
            
            assertThat(sheet.positions).containsAllEntriesOf(outSheet.positions);
            assertThat(sheet.labels).isNull();
            assertThat(outSheet.labels).isNull();
            
            assertThat(sheet.continueNumber)
                .containsExactly(1, 2, 3);
            
            assertThat(sheet.dateList)
            .containsExactly(LocalDate.of(2017, 5, 5), LocalDate.of(2017, 5, 6), LocalDate.of(2017, 5, 7));
            
        }
        
    }
    
    @XlsSheet(name="通常")
    private static class NormalSheet {
        
        private Map<String, CellPosition> positions;
        
        private Map<String, String> labels;
        
        @XlsArrayCell(address="B4", size=5)
        private List<String> horizontal1;
        
        @XlsArrayCell(column=1, row=5, size=3, direction=ArrayDirection.Vertical)
        @XlsDateConverter(excelPattern="yyyy\"年\"m\"月\"d\"日\"")
        private LocalDate[] vertical1;
        
        
    }
    
    /**
     * 不正なアノテーションの場合のテスト
     * <p>XMLによる属性変更で値を設定する
     */
    @XlsSheet(name="通常")
    private static class InvalidAnnoSheet {
        
        @XlsArrayCell(address="B4", size=5)
        private List<String> field1;
        
        private String field2;

    }
    
    @XlsSheet(name="結合の考慮")
    private static class ItemMergedSheet {
        
        private Map<String, CellPosition> positions;
        
        private Map<String, String> labels;
        
        @XlsArrayCell(address="B4", direction=ArrayDirection.Horizon, size=5, itemMerged=false)
        private List<String> hMergedFalse;
        
        @XlsArrayCell(address="B5", direction=ArrayDirection.Horizon, size=3, itemMerged=true)
        private List<String> hMergedTrue;
        
        @XlsArrayCell(address="C7", direction=ArrayDirection.Vertical, size=5, itemMerged=false)
        private List<String> vMergedFalse;
        
        @XlsArrayCell(address="D7", direction=ArrayDirection.Vertical, size=3, itemMerged=true)
        private List<String> vMergedTrue;
    }
    
    @XlsSheet(name="配列の操作(Horizon)")
    private static class HorizontalArrayOperatorSheet {
        
        private Map<String, CellPosition> positions;
        
        private Map<String, String> labels;
        
        @XlsArrayCell(address="A5", size=3, direction=ArrayDirection.Horizon)
        @XlsArrayOperator(overCase=OverOperate.Break)
        private List<String> overBreak;
        
        @XlsArrayCell(address="A8", size=3, direction=ArrayDirection.Horizon)
        @XlsArrayOperator(overCase=OverOperate.Error)
        private List<String> overError;
        
        @XlsArrayCell(address="A12", size=5, direction=ArrayDirection.Horizon)
        @XlsArrayOperator(remainedCase=RemainedOperate.None)
        private List<String> remainedNone;
        
        @XlsArrayCell(address="A15", size=5, direction=ArrayDirection.Horizon)
        @XlsArrayOperator(remainedCase=RemainedOperate.Clear)
        private List<String> remainedClear;
        
    }
    
    @XlsSheet(name="配列の操作(Vertical)")
    private static class VerticalArrayOperatorSheet {
        
        private Map<String, CellPosition> positions;
        
        private Map<String, String> labels;
        
        @XlsArrayCell(address="A5", size=3, direction=ArrayDirection.Vertical)
        @XlsArrayOperator(overCase=OverOperate.Break)
        private List<String> overBreak;
        
        @XlsArrayCell(address="D5", size=3, direction=ArrayDirection.Vertical)
        @XlsArrayOperator(overCase=OverOperate.Error)
        private List<String> overError;
        
        @XlsArrayCell(address="A11", size=5, direction=ArrayDirection.Vertical)
        @XlsArrayOperator(remainedCase=RemainedOperate.None)
        private List<String> remainedNone;
        
        @XlsArrayCell(address="D11", size=5, direction=ArrayDirection.Vertical)
        @XlsArrayOperator(remainedCase=RemainedOperate.Clear)
        private List<String> remainedClear;
        
    }
    
    @XlsSheet(name="変換処理")
    private static class ConverterSheet {
        
        private Map<String, CellPosition> positions;
        
        private Map<String, String> labels;
        
        @XlsArrayCell(address="A5", size=4)
        @XlsTrim
        private List<String> text;
        
        @XlsArrayCell(address="A8", size=4)
        @XlsBooleanConverter(loadForTrue="○", loadForFalse="×", saveAsTrue="○", saveAsFalse="×" )
        private List<Boolean> bool;
        
        @XlsArrayCell(address="A11", size=4)
        @XlsNumberConverter(javaPattern="###,##0", excelPattern="###,##0")
        @XlsDefaultValue("100")
        private List<Integer> number_int;
        
        @XlsArrayCell(address="F5", size=4, direction=ArrayDirection.Vertical)
        private double[] number_double;
        
    }
    
    @XlsSheet(name="数式を指定")
    private static class FormulaSheet {
        
        private Map<String, CellPosition> positions;
        
        private Map<String, String> labels;
        
        @XlsArrayCell(address="B5", size=3, direction=ArrayDirection.Vertical)
        @XlsFormula(value="ROW()-4", primary=true)
        private int[] continueNumber;
        
        @XlsArrayCell(address="D5", size=3)
        @XlsDateConverter(excelPattern="yyyy/m/d;@", javaPattern="yyyy/M/d")
        @XlsFormula("\\$I\\$5+{columnNumber}")
        private List<LocalDate> dateList;
    }
    
    
}
