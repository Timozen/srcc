call conda env remove -n srcc
call conda env create -n -y srcc python=3.7
call conda install -n srcc -y numpy
call conda install -n srcc -y scipy
call conda install -n srcc -y pandas
call conda install -n srcc -y matplotlib
call conda install -n srcc -y tensorflow-gpu
call conda install -n srcc -y keras