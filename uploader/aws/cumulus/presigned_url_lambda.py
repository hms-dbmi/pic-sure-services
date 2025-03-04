# IMPORTANT, READ
# This is a copy of the GeneratePresignedURL function in patnum_bucket.yaml
# If you change this, please copy it over
import json
import uuid
import logging
import os

import boto3
from botocore.exceptions import NoCredentialsError

site_name = 'bch'
valid_file_names = ['patients.txt', site_name + '_genotypic_data.tsv']
logger = logging.getLogger()
logger.setLevel('INFO')


def handler(event, context):
    permitted_bucket = os.environ['BUCKET_NAME']
    logging.info('Found permitted bucket name: %s', permitted_bucket)
    bucket_name = event.get('bucket_name')
    object_key = event.get('object_key')

    if not bucket_name or not object_key:
        return {
            'statusCode': 400,
            'body': json.dumps({'error': 'bucket_name and object_key are required'})
        }
    
    if bucket_name != permitted_bucket:
        return {
            'statusCode': 400,
            'body': json.dumps({'error': 'we cant upload to that bucket either'})
        }

    if not _validate_bucket_key(object_key):
        return {
            'statusCode': 400,
            'body': json.dumps({'error': 'object key must be a UUID directory followed by one of these files: {}'.format(valid_file_names)})
        }
    logging.info('Request to upload %s to %s is ok. Generating URL', object_key, bucket_name)
    
    s3_client = boto3.client('s3')
    try:
        presigned_url = s3_client.generate_presigned_url(
            ClientMethod='put_object',
            Params={
                'Bucket': bucket_name,
                'Key': object_key
            },
            ExpiresIn=3600  # URL expires in 1 hour (adjust as needed)
        )
        return {
            'statusCode': 200,
            'body': json.dumps({'presigned_url': presigned_url})
        }
    except NoCredentialsError:
        return {
            'statusCode': 500,
            'body': json.dumps({'error': 'Credentials not available'})
        }
    except Exception as e:
        return {
            'statusCode': 500,
            'body': json.dumps({'error': str(e)})
        }

def _validate_bucket_key(full_path: str) -> bool:
    dirs = full_path.split("/")
    return len(dirs) == 2 and str(uuid.UUID(dirs[0])) == dirs[0] and dirs[1] in valid_file_names