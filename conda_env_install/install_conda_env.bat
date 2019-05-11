conda env remove -n srcc
conda env create -n srcc python=3.7
conda install -n srcc -y numpy
conda install -n srcc -y scipy
conda install -n srcc -y pandas
conda install -n srcc -y matplotlib
conda install -n srcc -y tensorflow-gpu
conda install -n srcc -y keras