#!/bin/bash
function join_by { local IFS="$1"; shift; echo "$*"; }

mkdir -p results/

corpus=$1
format=$2
expandMappings=$3
ontologies=`join_by '_' ${@:5:$#}`
corpusname=`basename $corpus`
corpusdir=`dirname $corpus`
resdir="$ontologies"_em"$expandMappings"_"$format"_"$corpusname"`basename $corpusdir`

rm -rf results/$resdir

java -jar target/bpeval.jar $corpus results/$resdir $format $expandMappings $4 ${@:5:$#} > /dev/null

res1="$corpusname"`basename $corpusdir`_"$ontologies"_CUI_em"$expandMappings"_"$format"_EM.txt
java -cp brateval_orig.jar au.com.nicta.csp.brateval.CompareNotesEHealth results/$resdir  $corpus true > results/$res1

res2="$corpusname"`basename $corpusdir`_"$ontologies"_CUI_em"$expandMappings"_"$format"_PM.txt
java -cp brateval_orig.jar au.com.nicta.csp.brateval.CompareNotesEHealth results/$resdir  $corpus false > results/$res2


res3="$corpusname"`basename $corpusdir`_"$ontologies"_Semantic_em"$expandMappings"_"$format"_EM.txt
java -cp brateval_orig.jar au.com.nicta.csp.brateval.CompareEntities results/$resdir $corpus true >  results/$res3

res4="$corpusname"`basename $corpusdir`_"$ontologies"_Semantic_em"$expandMappings"_"$format"_PM.txt
java -cp brateval_orig.jar au.com.nicta.csp.brateval.CompareEntities results/$resdir $corpus false >  results/$res4


convert_output () {
	#We parse the last line of the scorer output that has the following format "Overall	149	151	1584	0.4967	0.0860	0.1466", in order the fields are Label, TP, FP, FN, P, R ,F1
	#The 4 first fields are set to "" so as to discard the label (Overall) and the TP/FP/FN values, then we capture the percentages for P, R and F1 and print them out as CSV
	echo "`tail -n1 results/$1 | awk '{$1=$2=$3=$4=""; print $0}' | sed 's/    \([01]\)\.\([0-9][0-9][0-9][0-9]\) \([01]\).\([0-9][0-9][0-9][0-9]\) \([01]\).\([0-9][0-9][0-9][0-9]\)/\1.\2,\3.\4,\5.\6/g'`"
}

# Printing Group EM, Group PM, CUI EM, CUI PM in order, each is computed from the result files produced above and whose path is respectively stored in $res3 $res4 $res1 and $res2
echo "`convert_output $res3`,`convert_output $res4`,`convert_output $res1`,`convert_output $res2`"
