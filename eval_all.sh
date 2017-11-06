#!/usr/bin/env bash

emeatest=../data/quaero/corpus/test/EMEA
emeadev=../data/quaero/corpus/dev/EMEA
emeatrain=../data/quaero/corpus/train/EMEA
medlinetest=../data/quaero/corpus/test/MEDLINE
medlinedev=../data/quaero/corpus/dev/MEDLINE
medlinetrain=../data/quaero/corpus/train/MEDLINE


./eval_quaero.sh $emeatest quaero false MSHFRE CIM-10 MDRFRE SNMIFRE MEDLINEPLUS MTHMSTFRE CIF WHO-ARTFRE CISP-2 ATCFRE
#./eval_quaero.sh $medlinetest quaeroimg false MSHFRE CIM-10 MDRFRE SNMIFRE MEDLINEPLUS MTHMSTFRE CIF WHO-ARTFRE CISP-2 ATCFRE
