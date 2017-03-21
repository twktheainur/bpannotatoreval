#!/bin/bash
function join_by { local IFS="$1"; shift; echo "$*"; }

mkdir -p results/

corpus=$1
format=$2
expandMappings=$3
ontologies=`join_by '_' ${@:4:$#}`
corpusname=`basename $corpus`
corpusdir=`dirname $corpus`
resdir="$ontologies"_em"$expandMappings"_"$format"_"$corpusname"`basename $corpusdir`

rm -rf results/$resdir

java -jar target/bpeval.jar $corpus results/$resdir $format $expandMappings ${@:4:$#} > /dev/null

res1="$corpusname"`basename $corpusdir`_"$ontologies"_CUI_em"$expandMappings"_"$format"_EM.txt
java -cp ../QUAERO_FrenchMed/eval/BRATeval-2015-04-24.jar au.com.nicta.csp.brateval.CompareNotesEHealth $corpus results/$resdir true >> results/$res1

res2="$corpusname"`basename $corpusdir`_"$ontologies"_CUI_em"$expandMappings"_"$format"_PM.txt
java -cp ../QUAERO_FrenchMed/eval/BRATeval-2015-04-24.jar au.com.nicta.csp.brateval.CompareNotesEHealth $corpus results/$resdir false >> results/$res2


res3="$corpusname"`basename $corpusdir`_"$ontologies"_Semantic_em"$expandMappings"_"$format"_EM.txt
java -cp ../QUAERO_FrenchMed/eval/BRATeval-2015-04-24.jar au.com.nicta.csp.brateval.CompareEntities results/$resdir $corpus true >>  results/$res3

res4="$corpusname"`basename $corpusdir`_"$ontologies"_Semantic_em"$expandMappings"_"$format"_PM.txt
java -cp ../QUAERO_FrenchMed/eval/BRATeval-2015-04-24.jar au.com.nicta.csp.brateval.CompareEntities results/$resdir $corpus false >>  results/$res4


echo "------------------$resdir-----------------------------"
echo "CUI EM `tail -n1 results/$res1 | awk '{$1=$2=$3=$4=""; print $0}'`"
echo "CUI PM `tail -n1 results/$res2 | awk '{$1=$2=$3=$4=""; print $0}'`"
echo "Semantic EM `tail -n1 results/$res3 | awk '{$1=$2=$3=$4=""; print $0}'`"
echo "Semantic PM `tail -n1 results/$res4 | awk '{$1=$2=$3=$4=""; print $0}'`"
