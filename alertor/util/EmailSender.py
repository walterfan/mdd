# !/usr/bin/env python

import os
import smtplib
import mimetypes
import logging
from email.mime.multipart import MIMEMultipart

from email import encoders

from email.mime.audio import MIMEAudio
from email.mime.base import MIMEBase
from email.mime.image import MIMEImage
from email.mime.text import MIMEText

from jinja2 import Template
import pprint
import FileLogger
import SearchConfig as cfg

pp = pprint.PrettyPrinter(indent=4)


class StderrLogger(object):

    def __init__(self):
        self.logger = logging.getLogger('mail')

    def write(self, message):
        self.logger.debug(message)


class EmailMessageBuilder:

    def __init__(self, tpl=cfg.get_home_path() + "/template/email_template.j2"):
        self.parameters = {}
        self.template_file = tpl
        self.mimeMsg = MIMEMultipart()

    def setFrom(self, strFrom):
        self.mimeMsg['From'] = strFrom

    def setTo(self, strArrTos):
        self.mimeMsg['To'] = ','.join(strArrTos)

    def setSubject(self, strSubject):
        self.mimeMsg['Subject'] = strSubject

    def attachText(self, text):
        mimeText = MIMEText(text, 'plain')
        self.mimeMsg.attach(mimeText)

    def attachHtml(self, html):
        mimeHtml = MIMEText(html, 'html')
        self.mimeMsg.attach(mimeHtml)

    def attachFile(self, filename):

        if not os.path.isfile(filename):
            return

        if (os.path.getsize(filename) == 0):
            return

        ctype, encoding = mimetypes.guess_type(filename)
        if ctype is None or encoding is not None:
            ctype = "application/octet-stream"

        maintype, subtype = ctype.split("/", 1)
        print('maintype={}, subtype={}'.format(maintype, subtype))

        attachment = None
        if maintype == "text":
            with open(filename, 'rb') as fp:
                attachment = MIMEText(fp.read(), _subtype=subtype, _charset='utf-8')
                # self.attachText(fp.read())

        elif maintype == "image":
            with open(filename, 'rb') as fp:
                attachment = MIMEImage(fp.read(), _subtype=subtype)

        elif maintype == "audio":
            with open(filename, 'rb') as fp:
                attachment = MIMEAudio(fp.read(), _subtype=subtype)

        else:
            with open(filename, 'rb') as fp:
                attachment = MIMEBase(maintype, subtype)
                attachment.set_payload(fp.read())
                encoders.encode_base64(attachment)

        if attachment:
            attachment.add_header("Content-Disposition", "attachment", filename=os.path.basename(filename))
            self.mimeMsg.attach(attachment)

    def attachImage(self, filename):
        print("attache %s" % filename)
        if not os.path.isfile(filename):
            print("attachImage {} not exist".format(filename))
            return
        else:
            print("attachImage {} begin".format(filename))

        with open(filename, 'rb') as fp:
            msgImage = MIMEImage(fp.read())
            msgImage.add_header('Content-ID', os.path.basename(filename))
            self.mimeMsg.attach(msgImage)

    def build(self, **kwargs):
        self.parameters.update(kwargs)

        with open(self.template_file, "r") as fp:
            template = Template(fp.read())
            emailBody = template.render(self.parameters)
            pp.pprint(emailBody)
            self.attachHtml(emailBody)

        return self.mimeMsg


class EmailSender:

    def __init__(self, defaultSender, mailServer, mailPort=25, needLogin=False, useTls=False, **kwargs):
        self.mailServer = mailServer
        self.mailPort = mailPort
        self.useTls = useTls
        self.debugLevel = 0
        self.username = kwargs.get('username', os.getenv('EMAIL_USER'))
        self.password = kwargs.get('password', os.getenv('EMAIL_PWD'))
        self.defaultSender = defaultSender

        self.needLogin = needLogin
        self.smtpServer = None

    def _send(self, mimeMsg):
        try:
            self.smtpServer = smtplib.SMTP(self.mailServer, self.mailPort)
            self.smtpServer.set_debuglevel(self.debugLevel)
            self.smtpServer.stderr = StderrLogger()

            if (self.needLogin):
                if (self.useTls):
                    self.smtpServer.ehlo()
                    self.smtpServer.starttls()
                    self.smtpServer.ehlo()

                self.smtpServer.login(self.username, self.password)
            # self.smtpServer.starttls()
            self.smtpServer.send_message(mimeMsg)

            print("Successfully sent email")
        except smtplib.SMTPException as e:
            print("Error: unable to send email: %s " % str(e))

        finally:
            if (self.smtpServer):
                self.smtpServer.quit()

    def send(self, subject, receivers, message, tmplate="template/email_template.j2"):
        builder = EmailMessageBuilder(tmplate)
        builder.setSubject(subject)
        builder.setFrom(self.defaultSender)
        builder.setTo(receivers)
        self._send(builder.build(message=message))

    def sendWithImages(self, subject, receivers, message, tpl="template/email_template.j2", imageFiles=[], files=[]):
        builder = EmailMessageBuilder(tpl)
        builder.setSubject(subject)
        builder.setFrom(self.defaultSender)
        builder.setTo(receivers)

        for imageFile in imageFiles:
            builder.attachImage(imageFile)

        for filename in files:
            builder.attachFile(filename)

        self._send(builder.build(message=message))
