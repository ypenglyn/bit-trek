# This Python file uses the following encoding: utf-8
from __future__ import unicode_literals
import json
import requests


class Solr(object):
    """
    Python implementation of the basic operation in the Solr API Rest
    """

    def __init__(self, url, timeout=5):
        """
        Create an instance of Solr class.

        Args:
            url: endpoint of Solr
            timeout: Time for any request, default: 5 seconds
        """
        self.url = url
        self.timeout = timeout

    def select(self, params, format='json'):
        """
        Search Solr, return URL and JSON response.

        Args:
            params: Dictionary parameters to Solr
            format: Format of return send to Solr, default=json
        Return:
            Solr response
        """
        params['wt'] = format

        response = requests.get(self.url + '/select?', params=params, timeout=self.timeout)

        return response.text

    def delete(self, query, commit=False):
        """
        Delete documents matching `query` from Solr.

        Args:
            query: Solr query string, see: https://wiki.apache.org/solr/SolrQuerySyntax
            commit: Boolean to carry out the operation
        Return:
            Solr response
        """
        params = {}

        if commit:
            params['commit'] = 'true'

        headers = {'Content-Type': 'text/xml; charset=utf-8'}
        data = '<delete><query>{0}</query></delete>'.format(query)

        response = requests.post(self.url + '/update?', params=params,
                                 headers=headers, data=data, timeout=self.timeout)

        return response.text

    def update(self, data, headers=None, commit=False):
        """
        Post list of docs to Solr.

        Args:
            commit: Boolean to carry out the operation
            headers: Dictionary content headers to send,
                        default={'Content-Type': 'text/xml; charset=utf-8'}
            data: XML or JSON send to Solr, XML ex.:
        XML:
            <add>
              <doc>
                <field name="id">XXX</field>
                <field name="field_name">YYY</field>
              </doc>
            </add>
        JSON:
            [
                {
                    "id":"1",
                    "ti":"This is just a test"
                },
                {...}
            ]
        Return:
            Solr response
        """
        params = {}
        if commit:
            params['commit'] = 'true'

        if not headers:
            headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}

        response = requests.post(self.url + '/update?', params=params,
                                 headers=headers, data=json.dumps(data), timeout=self.timeout)

        return response.text

    def commit(self, waitsearcher=False):
        """
        Commit uncommitted changes to Solr immediately, without waiting.
        :param waitsearcher: Boolean wait or not the Solr to execute
        :param return: Solr response
        """

        data = '<commit waitSearcher="' + str(waitsearcher).lower() + '"/>'
        headers = {'Content-Type': 'text/xml; charset=utf-8'}

        response = requests.post(self.url + '/update?', headers=headers,
                                 data=data, timeout=self.timeout)

        return response.text
