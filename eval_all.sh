#!/usr/bin/env bash

emeatest=../data/quaero/corpus/test/EMEA
emeadev=../data/quaero/corpus/dev/EMEA
emeatrain=../data/quaero/corpus/train/EMEA
medlinetest=../data/quaero/corpus/test/MEDLINE
medlinedev=../data/quaero/corpus/dev/MEDLINE
medlinetrain=../data/quaero/corpus/train/MEDLINE

emeatest_f=../data/quaero/corpus/test/EMEA_french_filtered
emeadev_f=../data/quaero/corpus/dev/EMEA_french_filtered
emeatrain_f=../data/quaero/corpus/train/EMEA_french_filtered
medlinetest_f=../data/quaero/corpus/test/MEDLINE_french_filtered
medlinedev_f=../data/quaero/corpus/dev/MEDLINE_french_filtered
medlinetrain_f=../data/quaero/corpus/train/MEDLINE_french_filtered

format=quaero
unique_groups=false
expand_mappings=false

corpus=$medlinedev

ontologies=(MSHFRE CIM-10 MDRFRE SNMIFRE MEDLINEPLUS MTHMSTFRE CIF WHO-ARTFRE CISP-2 ATCFRE)
#ontologies=(MSHFRE MDRFRE SNMIFRE MEDLINEPLUS MTHMSTFRE CIF CISP-2 ATCFRE)
eval_run () {
	./eval_quaero.sh $corpus $format $expand_mappings $unique_groups "$@"
}


run_combinations () {
	combinations=`./generate_combination_command_lines.py $1 symb ${ontologies[*]}`
     ./generate_combination_command_lines.py $1 "num" ${ontologies[*]} > `echo $2|cut -d'.' -f1`_$1_combinations
IFS='
'	
	length=`echo "$combinations"| wc -l`
	let "progress=0"
	for subset in $combinations; do
		pct_progress=`echo "$progress""*100"/"$length" | bc`
 		(eval_run "${subset[*]}" 1>> "$2"_combi_"$1".csv) | dialog --gauge "$1-combinations, now computing ${subset[*]}" 7 200 $pct_progress
		let "progress++"
		echo "$progress / $length = $pct_progress"
	done
	echo "Done!" | dialog --gauge "$1-combinations done!" 7 200 100
}


# for i in `seq 1 ${#ontologies[@]}`; do
# 	run_combinations $i $1
# done

#run_combinations 10 $1
#run_combinations 9 $1
#run_combinations 1 $1
#run_combinations 8 $1
#run_combinations 2 $1
#run_combinations 3 $1 #Some missing here
#run_combinations 4 $1 #Some missing here
run_combinations 5 $1
run_combinations 6 $1
run_combinations 7 $1




