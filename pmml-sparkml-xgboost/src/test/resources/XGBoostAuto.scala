import java.nio.file.{Files, Paths}

import ml.dmlc.xgboost4j.scala.spark.XGBoostRegressor
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature._
import org.apache.spark.sql.types.StringType
import org.jpmml.sparkml.PMMLBuilder
import org.jpmml.sparkml.xgboost.SparseToDenseTransformer

var df = spark.read.option("header", "true").option("inferSchema", "true").csv("csv/Auto.csv")
df = df.withColumn("originTmp", df("origin").cast(StringType)).drop("origin").withColumnRenamed("originTmp", "origin")

val cat_cols = Array("cylinders", "model_year", "origin")
val cont_cols = Array("acceleration", "displacement", "horsepower", "weight")

val indexer = new StringIndexer().setInputCols(cat_cols).setOutputCols(cat_cols.map(cat_col => "idx_" + cat_col))
val ohe = new OneHotEncoder().setDropLast(false).setInputCols(indexer.getOutputCols).setOutputCols(cat_cols.map(cat_col => "ohe_" + cat_col))
val assembler = new VectorAssembler().setInputCols(ohe.getOutputCols ++ cont_cols).setOutputCol("featureVector")

val sparse2dense = new SparseToDenseTransformer().setInputCol(assembler.getOutputCol).setOutputCol("denseFeatureVec")

val regressor = new XGBoostRegressor(Map("objective" -> "reg:squarederror", "num_round" -> 101, "num_workers" -> 1, "skip_clean_checkpoint" -> true)).setLabelCol("mpg").setFeaturesCol(sparse2dense.getOutputCol)

val pipeline = new Pipeline().setStages(Array(indexer, ohe, assembler, sparse2dense, regressor))
val pipelineModel = pipeline.fit(df)

var xgbDf = pipelineModel.transform(df)
xgbDf = xgbDf.selectExpr("prediction as mpg")
xgbDf.coalesce(1).write.format("com.databricks.spark.csv").option("header", "true").save("csv/XGBoostAuto")

pipelineModel.save("pipeline/XGBoostAuto")

//val pmmlBytes = new PMMLBuilder(df.schema, pipelineModel).buildByteArray()
//Files.write(Paths.get("pmml/XGBoostAuto.pmml"), pmmlBytes)
