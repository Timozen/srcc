call conda remove -n srcc -y
call conda create -n srcc python=3.7 -y
call conda install -n srcc -y numpy
call conda install -n srcc -y scipy
call conda install -n srcc -y pandas
call conda install -n srcc -y matplotlib
call conda install -n srcc -y tensorflow-gpu
call conda install -n srcc -y keras
call conda install --name srcc -y autopep8
call conda install --name srcc -y pylint