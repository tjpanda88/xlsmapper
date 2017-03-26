package com.gh.mygreen.xlsmapper.fieldaccessor;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;
import org.junit.Before;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.gh.mygreen.xlsmapper.util.CellAddress;

/**
 * {@link MapColumnPositionSetterFactory}のテスタ
 *
 * @since 2.0
 * @author T.TSUCHIE
 *
 */
@RunWith(Enclosed.class)
public class MapColumnPositionSetterFactoryTest {
    
    /**
     * 位置情報が無い場合
     *
     */
    public static class NotPosition {
        
        private MapColumnPositionSetterFactory setterFactory;
        
        @Before
        public void setUp() throws Exception {
            this.setterFactory = new MapColumnPositionSetterFactory();
        }
        
        @Test
        public void testCreate() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(SampleRecord.class, "test");
            assertThat(positionSetter).isEmpty();
            
        }
        
        private static class SampleRecord {
            
        }
    
    }
    
    /**
     * マップ型のフィールドの位置情報の場合
     *
     */
    public static class ByMapField {
        
        private MapColumnPositionSetterFactory setterFactory;
        
        @Before
        public void setUp() throws Exception {
            this.setterFactory = new MapColumnPositionSetterFactory();
        }
        
        /**
         * フィールドのタイプがMapではない場合
         */
        @Test
        public void testCreateWithNoMap() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(NoMapRecord.class, "test");
            assertThat(positionSetter).isEmpty();
            
        }
        
        /**
         * マップのGenericsのクラスタイプがサポート対象外の場合
         */
        @Test
        public void testCreateWithNoSupportType() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(NoSupportTypeRecord.class, "test");
            assertThat(positionSetter).isEmpty();
            
        }
        
        /**
         * マップのタイプが{@link CellAddress}の場合
         */
        @Test
        public void testCreateWithCellAddress() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(CellAddressRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            {
                // 位置情報を設定する
                CellAddressRecord record = new CellAddressRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.positions)
                    .hasSize(1)
                    .containsEntry("test[abc]", position);
            }
            
        }
        
        /**
         * マップのタイプが{@link Point}の場合
         */
        @Test
        public void testCreateWithPoint() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(PointRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            
            {
                // 位置情報を設定する
                PointRecord record = new PointRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.positions)
                    .hasSize(1)
                    .containsEntry("test[abc]", position.toPoint());
            }
            
        }
        
        /**
         * マップのタイプが{@link Point}の場合
         */
        @Test
        public void testCreateWithPoiCellAddress() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(PoiCellAddressRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            
            {
                // 位置情報を設定する
                PoiCellAddressRecord record = new PoiCellAddressRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.positions)
                    .hasSize(1)
                    .containsEntry("test[abc]", position.toPoiCellAddress());
            }
            
        }
        
        /**
         * Map形式ではない場合
         *
         */
        private static class NoMapRecord {
            
            CellAddress positions;
            
        }
        
        /**
         * 値がサポートしていないクラスタイプの場合
         *
         */
        private static class NoSupportTypeRecord {
            
            Map<String, CellReference> positions;
            
        }
        
        private static class CellAddressRecord {
            
            Map<String, CellAddress> positions;
            
        }
        
        private static class PointRecord {
            
            Map<String, Point> positions;
            
        }
        
        private static class PoiCellAddressRecord {
            
            Map<String, org.apache.poi.ss.util.CellAddress> positions;
            
        }
        
    }
    
    
    /**
     * メソッドによる位置情報を格納する場合
     *
     */
    public static class ByMethod {
        
        private MapColumnPositionSetterFactory setterFactory;
        
        @Before
        public void setUp() throws Exception {
            this.setterFactory = new MapColumnPositionSetterFactory();
        }
        
        @Test
        public void testCreateWithCellAddress() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(CellAddressRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            {
                // 位置情報を設定する
                CellAddressRecord record = new CellAddressRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.addressMap)
                    .hasSize(1)
                    .containsEntry("abc", position);
            }
            
        }
        
        @Test
        public void testCreateWithPoint() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(PointRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            {
                // 位置情報を設定する
                PointRecord record = new PointRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.addressMap)
                    .hasSize(1)
                    .containsEntry("abc", position.toPoint());
            }
            
        }
        
        @Test
        public void testCreateWithInt() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(IntRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            {
                // 位置情報を設定する
                IntRecord record = new IntRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.addressMap)
                    .hasSize(1)
                    .containsEntry("abc", position.toPoint());
            }
            
        }
        
        @Test
        public void testCreateWithPoiCellAddress() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(PoiCellAddressRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            {
                // 位置情報を設定する
                PoiCellAddressRecord record = new PoiCellAddressRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.addressMap)
                    .hasSize(1)
                    .containsEntry("abc", position.toPoiCellAddress());
            }
            
        }
        
        private static class CellAddressRecord {
            
            private Map<String, CellAddress> addressMap = new HashMap<>();
            
            public void setTestPosition(String key, CellAddress address) {
                this.addressMap.put(key, address);
            }
            
        }
        
        private static class PointRecord {
            
            private Map<String, Point> addressMap = new HashMap<>();
            
            void setTestPosition(String key, Point address) {
                this.addressMap.put(key, address);
            }
            
        }
        
        private static class IntRecord {
            
            private Map<String, Point> addressMap = new HashMap<>();;
            
            void setTestPosition(String key, int column, int row) {
                this.addressMap.put(key, new Point(column, row));
            }
            
        }
        
        private static class PoiCellAddressRecord {
            
            private Map<String, org.apache.poi.ss.util.CellAddress> addressMap = new HashMap<>();
            
            protected void setTestPosition(String key, org.apache.poi.ss.util.CellAddress address) {
                this.addressMap.put(key, address);
            }
            
        }
        
    }
    
    /**
     * フィールドによる位置情報を格納する場合
     *
     */
    public static class ByField {
        
        private MapColumnPositionSetterFactory setterFactory;
        
        @Before
        public void setUp() throws Exception {
            this.setterFactory = new MapColumnPositionSetterFactory();
        }
        
        @Test
        public void testCreateWithCellAddress() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(CellAddressRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            {
                // 位置情報を設定する
                CellAddressRecord record = new CellAddressRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.testPosition)
                    .hasSize(1)
                    .containsEntry("abc", position);
            }
            
        }
        
        @Test
        public void testCreateWithPoint() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(PointRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            {
                // 位置情報を設定する
                PointRecord record = new PointRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.testPosition)
                    .hasSize(1)
                    .containsEntry("abc", position.toPoint());
            }
            
        }
        
        @Test
        public void testCreateWithPoiCellAddress() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(PoiCellAddressRecord.class, "test");
            assertThat(positionSetter).isNotEmpty();
            
            {
                // 位置情報を設定する
                PoiCellAddressRecord record = new PoiCellAddressRecord();
                
                MapColumnPositionSetter accessor = positionSetter.get();
                CellAddress position = CellAddress.of("B24");
                
                accessor.set(record, position, "abc");
                
                assertThat(record.testPosition)
                    .hasSize(1)
                    .containsEntry("abc", position.toPoiCellAddress());
            }
            
        }
        
        @Test
        public void testCreateWithNotSupportType() {
            
            Optional<MapColumnPositionSetter> positionSetter = setterFactory.create(NotSupportTypeRecord.class, "test");
            assertThat(positionSetter).isEmpty();
            
        }
        
        private static class CellAddressRecord {
            
            private Map<String, CellAddress> testPosition;
            
            
        }
        
        private static class PointRecord {
            
            private Map<String, Point> testPosition;
            
            
        }
        
        private static class PoiCellAddressRecord {
            
            private Map<String, org.apache.poi.ss.util.CellAddress> testPosition;
            
            
        }
        
        private static class NotSupportTypeRecord {
            
            private Map<String, Cell> testPosition;
            
            
        }
    }

}