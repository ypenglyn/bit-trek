# This Python file uses the following encoding: utf-8
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

# Imports
import json
import tensorflow as tf
import requests as req
from app.solr import Solr

tf.logging.set_verbosity(tf.logging.INFO)
# NOTE: change hostname to yours
SOLR_INDEXING_END_POINT = 'http://172.17.0.1:8983/solr/bits'
HASHER_END_POINT = 'http://172.17.0.1:8080/hash'


def main(unused_argv):
    # Load training and eval data
    mnist = tf.contrib.learn.datasets.load_dataset("mnist")
    train_data = mnist.train.images  # Returns np.array
    print(train_data.shape)

    solr = Solr(SOLR_INDEXING_END_POINT, timeout=100)
    solr.delete('*:*')

    counter = 0
    chunk = []
    for row in train_data.tolist():
        chunk.append(row)
        counter += 1
        if counter != 0 and counter % 50 == 0:
            print('Current indexig offset is {idx}.'.format(idx=counter))
            hashed_chunk = hash(chunk, counter - 50)
            solr.update(hashed_chunk)
            solr.commit()
            chunk[:] = []
    solr.commit()


def hash(data, offset=0):
    json_fmt = json.dumps(data)
    r = req.post(HASHER_END_POINT, data={'value': json_fmt})

    bits = json.loads(r.text)
    result_list = []
    data_size = len(data)
    for i in range(0, data_size):
        result_list.append({'id': str(offset + i), 'bits': bits[i]})
    return result_list


if __name__ == "__main__":
    tf.app.run()
