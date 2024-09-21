import os, sys
from dotenv import load_dotenv
import os

current_path = os.path.dirname(os.path.abspath(__file__))
lib_path = os.path.join(current_path, "../util")
sys.path.append(lib_path)

import EmailSender
import FileLogger

# Load environment variables from the .env file (if present)
load_dotenv()

def quickTest(emailSender, emailReceiver, **kwargs):
    recipients = [emailReceiver]

    mdLogger = FileLogger.MarkdownLogger('test.md')

    mdLogger.print("# Pager duty alert test email")

    mdLogger.print("\n\n")
    mdLogger.printTableTitle(['date', 'pool', 'error count'])
    mdLogger.printTableRow(['2018-8-1', 'east-china', 3000])
    mdLogger.printTableRow(['2018-8-2', 'east-china', 4000])
    mdLogger.printTableRow(['2018-8-3', 'east-china', 5000])
    mdLogger.printTableRow(['2018-8-4', 'east-china', 6000])
    mdLogger.printTableRow(['2018-8-5', 'east-china', 7000])

    mdLogger.print("\n\n")

    sender = EmailSender.EmailSender(emailSender, **kwargs)

    sender.sendWithImages("Production Alert Email", recipients, mdLogger.toHtml(), "../template/email_template.j2")


if __name__ == '__main__':
    quickTest(os.getenv('MAIL_SENDER'), os.getenv('MAIL_RECEIVER'), \
              mailServer=os.getenv('MAIL_SERVER'), \
              mailPort=os.getenv('MAIL_PORT'),\
              needLogin=True,\
              useTls=True,\
              username=os.getenv('MAIL_USERNAME'),\
              password=os.getenv('MAIL_PASSWORD'))
