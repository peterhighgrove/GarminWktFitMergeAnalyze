/////////////////////////////////////////////////////////////////////////////////////////////
// Copyright 2024 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/////////////////////////////////////////////////////////////////////////////////////////////
// ****WARNING****  This file is auto-generated!  Do NOT edit this file.
// Profile Version = 21.141.0Release
// Tag = production/release/21.141.0-0-g2aa27e1
/////////////////////////////////////////////////////////////////////////////////////////////


package com.garmin.fit.csv;

import com.garmin.fit.*;

import java.io.ByteArrayOutputStream;
import java.util.Collection;

public class MesgCSVWriter extends MesgCSVWriterBase implements MesgListener, MesgDefinitionListener {
    private int numUnknownMesgs = 0;
    private int numUnknownFields = 0;
    private boolean dataInCsv;


    public MesgCSVWriter(ByteArrayOutputStream byteArrayOutputStream) {
        super(byteArrayOutputStream);
    }

    public void close() {
        csv.close();
    }

    public int getNumUnknownMesgs() {
        return numUnknownMesgs;
    }

    public int getNumUnknownFields() {
        return numUnknownFields;
    }

    public void onMesgDefinition(MesgDefinition mesgDef) {
        Collection<FieldDefinition> fields = mesgDef.getFields();
        int headerNum;
        Mesg mesg = Factory.createMesg(mesgDef.getNum());
        if (hideUnknownData && mesg.getName().equals("unknown")) {
            return;
        }

        csv.clear();
        csv.set("Type", "Definition");
        csv.set("Local Number", mesgDef.getLocalNum());

        csv.set("Message", mesg.getName());

        headerNum = 0;

        for (FieldDefinition fieldDef : fields) {
            Field field = Factory.createField(mesgDef.getNum(), fieldDef.getNum());
            if (hideUnknownData && field.getName().equals("unknown")) {
                numUnknownFields++;
                continue;
            }

            headerNum++;

            csv.set("Field " + headerNum, field.getName());

            csv.set("Value " + headerNum, fieldDef.getSize() / Fit.baseTypeSizes[fieldDef.getType() & Fit.BASE_TYPE_NUM_MASK]);
            csv.set("Units " + headerNum, "");
        }

        for (DeveloperFieldDefinition fieldDef : mesgDef.getDeveloperFields()) {
            if (hideUnknownData && !fieldDef.isDefined()) {
                numUnknownFields++;
                continue;
            }

            headerNum++;

            if (!fieldDef.isDefined()) {
                csv.set("Field " + headerNum, "undefined-dev-data");
            }
            else {
                csv.set("Field " + headerNum, fieldDef.getFieldName());
            }

            csv.set("Value " + headerNum, fieldDef.getSize() / Fit.baseTypeSizes[fieldDef.getType() & Fit.BASE_TYPE_NUM_MASK]);
            csv.set("Units " + headerNum, "");
        }

        csv.writeln();
        dataInCsv = true;
    }

    public void onMesg(Mesg mesg) {
        Collection<Field> fields = mesg.getFields();
        int headerNum;

        if ((mesg.getName().equals("unknown")) && (hideUnknownData)) {
            numUnknownMesgs++;
            return;
        }

        csv.clear();
        csv.set("Type", "Data");
        csv.set("Local Number", mesg.getLocalNum());
        csv.set("Message", mesg.getName());

        if (removeExpandedFields) {
            mesg.removeExpandedFields();
        }

        headerNum = 0;

        for (Field field : fields) {
            int subFieldIndex = mesg.getActiveSubFieldIndex(field.getNum());

            if (((field.getName().equals("unknown")) && (hideUnknownData))) {
                continue;
            }

            headerNum++;

            csv.set("Field " + headerNum, field.getName(subFieldIndex));

            String value = null;

            if (null == value) {
                value = getValueString(field, subFieldIndex);
            }

            csv.set("Value " + headerNum, value);
            csv.set("Units " + headerNum, formatUnits(field.getUnits(), field.getProfileType().name()));
        }

        for (DeveloperField field : mesg.getDeveloperFields()) {
            if (!field.isDefined() && hideUnknownData) {
                continue;
            }

            headerNum++;

            csv.set("Field " + headerNum, field.getName());
            String value = getValueString(field, Fit.SUBFIELD_INDEX_MAIN_FIELD);

            csv.set("Value " + headerNum, value);
            csv.set("Units " + headerNum, formatUnits(field.getUnits()));
        }

        csv.writeln();
        dataInCsv = true;
    }

    public boolean csvHasData() {
        return dataInCsv;
    }

}
