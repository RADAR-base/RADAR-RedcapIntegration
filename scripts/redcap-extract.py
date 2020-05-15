#!/usr/bin/env python3
import requests
import io
import csv
from ftplib import FTP
from datetime import datetime

def extract_to_bytes(extract, fields=None):
    """ Converts the REDCAP API response data to a io bytes handle
    Paramters
    _________
    extract: list of dicts
    fields: list of str
        Field names to include in CSV.

    Returns
    _______
    fb : io.BytesIO

    """
    return iostr_to_iobytes(write_csv(io.StringIO(''), extract, fields))

def write_csv(f, extract, fields=None):
    """ Writers extract to file handle
    Parameters
    __________
    f : file handle (string mode)
    extract: list of dicts
    fields: list of str
        Field names to include in CSV.

    Returns
    _______
    f : file handle (string mode)
    """
    keys = fields if fields is not None else extract[0].keys()
    dict_writer = csv.DictWriter(f, keys, extrasaction='ignore')
    dict_writer.writeheader()
    dict_writer.writerows(extract)
    return f

def iostr_to_iobytes(fs):
    """ Converts io.StringIO object to io.BytesIO
    Paramters
    _________
    fs : io.StringIO

    Returns
    _______
    fb : io.BytesIO
    """
    fs.flush()
    fs.seek(0)
    fb = io.BytesIO(fs.read().encode())
    return fb

def bytes_to_ftp(f, project, name, ip, username='', password=''):
    """ Saves file to the REDCAP extracts folder on RADAR-FTP.
    Parameters
    __________
    f : file handle (bytes)
    project : string
        Name of the project folder to save under
    name : string
        Name to give the file
    """
    ftp = FTP(ip)
    if (username and password):
        ftp.login(username, password)
    ftp.storbinary('STOR /RADAR-CNS/REDCAP/{}/{}'.format(project, name), f)
    ftp.close()
    return

def save_extract_ftp(extract, ip, project, *args, **kwargs):
    date = datetime.now()
    name = 'REDCAP_{}_{}.csv'.format(project, date.strftime('%Y%m%d_%H%M'))
    fields = kwargs.pop('fields', None)
    f = extract_to_bytes(extract, fields)
    bytes_to_ftp(f, project, name, ip, *args, **kwargs)
    return

def call_api(url, payload, **kwargs):
    r = requests.post(url, data=payload, **kwargs)
    r.raise_for_status()
    return r.json()

def get_redcap_metadata(url, token):
    payload = {'token': token,
               'format': 'json',
               'content': 'metadata',
               'type': 'flat'}
    return call_api(url, payload)

def get_redcap_extract(url, token):
    payload = {'token': token,
               'format': 'json',
               'content': 'record',
               'type': 'flat',
               'exportSurveyFields': 'true'}
    return call_api(url, payload)

def field_filter(entry, exclude_notes=False):
    blacklist_fields = ('landline', 'giffgaff_password', 'fitbit_password')
    inclusion_rules = (entry['identifier'] == '',)
    exclusion_rules = [entry['field_name'] in blacklist_fields]
    if exclude_notes:
        exclusion_rules.append(entry['field_type'] == 'notes')
    if all(inclusion_rules) and not any(exclusion_rules):
        return True
    return False

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(
            description='Extract data from RADAR Redcap server')
    parser.add_argument('project', type=str,
            help='The name of the RADAR project. The FTP folder name')
    parser.add_argument('token', type=str,
            help='The Redcap project API token')
    parser.add_argument('--redcap-url', type=str,
            help='The URL of the Redcap api endpoint',
            default='https://127.0.0.1/redcap/api/')
    parser.add_argument('--ftp-ip', type=str,
            help='The FTP IP to upload to',
            default='127.0.0.1')
    parser.add_argument('--ftp-user', type=str,
            help='The FTP username. If not set, tries to use .netrc',
            default='')
    parser.add_argument('--ftp-password', type=str,
            help='The FTP account password. Uses .netrc if --ftp-user is empty',
            default='')
    parser.add_argument('--exclude-notes', action='store_true',
            help='A flag; if present, "notes" fields will not be included',
            default=False)
    opts = parser.parse_args()
    if not (opts.ftp_user and opts.ftp_password):
        from netrc import netrc
        nrc = netrc()
        opts.ftp_user, _, opts.ftp_password = nrc.authenticators(opts.ftp_ip)

    metadata = get_redcap_metadata(opts.redcap_url,
                                   opts.token)
    extract = get_redcap_extract(opts.redcap_url,
                                 opts.token)
    fields = []
    metadata_dict = {f['field_name']: f for f in metadata}
    for f in extract[0].keys():
        if f in metadata_dict:
            if field_filter(metadata_dict[f], exclude_notes=opts.exclude_notes):
                fields.append(f)
        else:
            fields.append(f)
    save_extract_ftp(extract, ip=opts.ftp_ip,
                     username=opts.ftp_user, password=opts.ftp_password,
                     fields=fields, project=opts.project)
