package com.gh.mygreen.xlsmapper.cellconverter.impl;

import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import com.gh.mygreen.xlsmapper.XlsMapperConfig;
import com.gh.mygreen.xlsmapper.annotation.XlsTrim;
import com.gh.mygreen.xlsmapper.cellconverter.AbstractCellConverter;
import com.gh.mygreen.xlsmapper.cellconverter.TypeBindException;
import com.gh.mygreen.xlsmapper.fieldaccessor.FieldAccessor;


/**
 * String型を処理するためのConverter.
 * 
 * @version 2.0
 * @author T.TSUCHIE
 *
 */
public class StringCellConverter extends AbstractCellConverter<String> {

    @Override
    protected String parseDefaultValue(final String strValue, final FieldAccessor accessor,
            final XlsMapperConfig config) throws TypeBindException {
        
        return strValue;
    }
    
    @Override
    protected String parseCell(final Cell evaluatedCell, final String formattedValue, final FieldAccessor accessor,
            final XlsMapperConfig config) throws TypeBindException {
        
        if(formattedValue.isEmpty() && !accessor.hasAnnotation(XlsTrim.class)) {
            // トリムを行わない場合は、空文字をnullに補完する。
            return null;
        }
        
        return formattedValue;
    }
    
    @Override
    protected void setupCell(final Cell cell, final Optional<String> cellValue, final FieldAccessor accessor,
            final XlsMapperConfig config) throws TypeBindException {
        
        if(cellValue.isPresent() && !cellValue.get().isEmpty()) {
            cell.setCellValue(cellValue.get());
            
        } else {
            cell.setCellType(CellType.BLANK);
        }
        
    }
    
}