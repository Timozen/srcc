conda env remove -n srcc
conda create -n srcc -y python=3.7
conda install -n srcc -y numpy
conda install -n srcc -y scipy
conda install -n srcc -y pandas
conda install -n srcc -y matplotlib
conda install -n srcc -y tensorflow-gpu
conda install -n srcc -y keras
conda install -n srcc -y autopep8
conda install -n srcc -y pylint
conda install -n srcc -c anaconda -y flask 
conda install -n srcc -c conda-forge -y flask-restful 
conda install -n srcc -c anaconda -y requests
conda install -n srcc -c anaconda -y pillow 
