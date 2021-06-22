/*
 * Copyright (c) 2016 Villu Ruusmann
 *
 * This file is part of JPMML-SparkML
 *
 * JPMML-SparkML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SparkML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SparkML.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.sparkml.feature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.spark.ml.feature.Binarizer;
import org.dmg.pmml.Apply;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMMLFunctions;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.IndexFeature;
import org.jpmml.converter.PMMLUtil;
import org.jpmml.sparkml.FeatureConverter;
import org.jpmml.sparkml.SparkMLEncoder;

public class BinarizerConverter extends FeatureConverter<Binarizer> {

	public BinarizerConverter(Binarizer transformer){
		super(transformer);
	}

	@Override
	public List<Feature> encodeFeatures(SparkMLEncoder encoder){
		Binarizer transformer = getTransformer();

		Feature feature = encoder.getOnlyFeature(transformer.getInputCol());

		ContinuousFeature continuousFeature = feature.toContinuousFeature();

		Apply apply = PMMLUtil.createApply(PMMLFunctions.IF,
			PMMLUtil.createApply(PMMLFunctions.LESSOREQUAL, continuousFeature.ref(), PMMLUtil.createConstant(transformer.getThreshold())),
			PMMLUtil.createConstant(0d),
			PMMLUtil.createConstant(1d)
		);

		DerivedField derivedField = encoder.createDerivedField(formatName(transformer), OpType.CATEGORICAL, DataType.DOUBLE, apply);

		return Collections.singletonList(new IndexFeature(encoder, derivedField, Arrays.asList(0d, 1d)));
	}
}