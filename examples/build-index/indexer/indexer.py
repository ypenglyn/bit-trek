# This Python file uses the following encoding: utf-8
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

# Imports
import json
import tensorflow as tf
import requests as req
import numpy as np
import base64
from app.solr import Solr

tf.logging.set_verbosity(tf.logging.INFO)
# NOTE: change hostname to yours
SOLR_INDEXING_END_POINT = 'http://172.17.0.1:8983/solr/bits'
HASHER_END_POINT = 'http://172.17.0.1:8080/quantize'


def main(unused_argv):
    # Load training and eval data
    mnist = tf.contrib.learn.datasets.load_dataset("mnist")
    train_data = mnist.train.images
    train_label = mnist.train.labels.tolist()
    print(train_data.shape)

    solr = Solr(SOLR_INDEXING_END_POINT, timeout=100)
    solr.delete('*:*')

    # resize batch size in case of large dimension vector
    batch_size = 20
    chunk = []
    chunk_label = []
    for idx in range(1, train_data.shape[0] + 1):
        chunk.append(norm(train_data[idx - 1]).tolist())
        chunk_label.append(train_label[idx - 1])
        if idx != 0 and idx % batch_size == 0:
            print('Current indexig offset is {idx}.'.format(idx=idx))
            hashed_chunk = hash(chunk, chunk_label, idx - batch_size - 1)
            solr.update(hashed_chunk)
            solr.commit()
            chunk[:] = []
            chunk_label[:] = []

    solr.commit()


def norm(v):
    n = np.linalg.norm(v)
    return v / n


def hash(data, data_label, offset=0):
    json_fmt = json.dumps(data)
    r = req.post(HASHER_END_POINT, data={'value': json_fmt})

    bits = json.loads(r.text)
    result_list = []
    data_size = len(data)
    for i in range(0, data_size):
        scode = bits[i].get('scode')
        mcode = bits[i].get('mcode')
        lcode = bits[i].get('lcode')
        result_list.append({'id': str(offset + i) + '_' + str(data_label[i]), 'bits': scode, 'q1': mcode, 'q2': lcode})
    return result_list


if __name__ == "__main__":
    tf.app.run()
