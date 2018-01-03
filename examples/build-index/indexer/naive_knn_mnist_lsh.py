# This Python file uses the following encoding: utf-8
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

# Imports
import json
import tensorflow as tf
import requests as req
import numpy as np
from app.solr import Solr
from collections import Counter
from sklearn.metrics import precision_recall_fscore_support

tf.logging.set_verbosity(tf.logging.INFO)
# NOTE: change hostname to yours
SOLR_INDEXING_END_POINT = 'http://localhost:8983/solr/bits'
HASHER_END_POINT = 'http://localhost:8080/hash'


def main(unused_argv):
    # Load training and eval data
    mnist = tf.contrib.learn.datasets.load_dataset("mnist")
    test_data = mnist.test.images
    test_label = mnist.test.labels.tolist()
    print(test_data.shape)

    solr = Solr(SOLR_INDEXING_END_POINT, timeout=100)

    # resize batch size in case of large dimension vector
    batch_size = 20
    hashed = []
    chunk = []
    for idx in range(1, test_data.shape[0] + 1):
        chunk.append(norm(test_data[idx - 1]).tolist())
        if idx != 0 and idx % batch_size == 0:
            print('Current indexig offset is {idx}.'.format(idx=idx))
            hashed_chunk = hash(chunk, idx - batch_size - 1)
            chunk[:] = []
            hashed.extend(hashed_chunk)

    print(len(hashed))

    n_neighbors = 5
    predicts = []
    qtime_list = []
    hits_list = []
    use_index = False
    for idx in range(0, len(hashed)):
        params = {'fl': 'id, $x',
                  'x': 'cos(bits, {bits})'.format(bits=hashed[idx]),
                  'sort': '$x desc',
                  'rows': n_neighbors,
                  'cache': 'false',
                  'q': 'bits_match_3:"{bits}"'.format(bits=hashed[idx]),
                  'fq': '-filter(bits:"{bits}")'.format(bits=hashed[idx])}
        if use_index:
            params['q'] = '*:*'
        resposne = solr.select(params)
        nn = json.loads(resposne)

        qtime_list.append(int(nn.get('responseHeader').get('QTime')))
        hits_list.append(int(nn.get('response').get('numFound')))

        predict_list = nn.get('response').get('docs')
        if len(predict_list) > 0:
            nn_labels = list(r.get('id').split('_')[1] for r in predict_list)
            nn_label_predict = Counter(nn_labels).most_common(1)[0][0]
            predicts.append(int(nn_label_predict))
        else:
            predicts.append(-1)

    test_predict = np.asarray(predicts, dtype=np.uint8)
    result = precision_recall_fscore_support(mnist.test.labels, test_predict, average='micro')
    print(result)


def norm(v):
    n = np.linalg.norm(v)
    return v / n


def hash(data, offset=0):
    json_fmt = json.dumps(data)
    r = req.post(HASHER_END_POINT, data={'value': json_fmt})

    bits = json.loads(r.text)
    result_list = []
    data_size = len(data)
    for i in range(0, data_size):
        result_list.append(bits[i])
    return result_list


if __name__ == "__main__":
    tf.app.run()
