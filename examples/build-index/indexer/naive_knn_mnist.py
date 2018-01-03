# This Python file uses the following encoding: utf-8
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

# Imports
import tensorflow as tf
from sklearn import neighbors
from sklearn.metrics import precision_recall_fscore_support

tf.logging.set_verbosity(tf.logging.INFO)


def main(unused_argv):
    # Load training and eval data
    mnist = tf.contrib.learn.datasets.load_dataset("mnist")
    train_data = mnist.train.images
    train_label = mnist.train.labels
    test_data = mnist.test.images
    test_label = mnist.test.labels

    weights = 'distance'
    for n_neighbors in [1, 5, 10]:
        clf = neighbors.KNeighborsClassifier(n_neighbors, weights=weights)
        clf.fit(train_data, train_label)
        test_predict = clf.predict(test_data)
        print('precision_recall_fscore when weights = {w} and # of neighbor = {nn}'.format(w=weights, nn=n_neighbors))
        precision_recall_fscore_support(test_label, test_predict, average='micro')


if __name__ == "__main__":
    tf.app.run()
