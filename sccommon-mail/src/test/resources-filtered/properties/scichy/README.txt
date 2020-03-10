Aby uruchomić testy potrzebujesz dwóch plików:
########################
mail.test.data.properties (UTF-8)
########################
-------------
#Test1 wysłanie obrazka
mail.data.test1.from=scichy@ibpm.pro
mail.data.test1.replyTo=
mail.data.test1.to=Sławomir Cichy <slawas@slawas.pl>
mail.data.test1.cc=
mail.data.test1.bcc=
mail.data.test1.subject=Test wysyłania maila z afrykańskim słoniem
mail.data.test1.txt=Test wiadomości z załącznikiem.<br/>Polskie znaczki powinny być poprawne: ąśćęĄŚĘŃŹŻ :)<br/><b>pozdrawiam</b><br/> Slawas
mail.data.test1.attachments=src/test/resources/Afrykański-słoń-wielkość.jpg

#Test2 wysłanie pustego załącznika z ustawioną stroną kodową
mail.data.test2.from=scichy@ibpm.pro
mail.data.test2.replyTo=
mail.data.test2.to=Sławomir Cichy <slawas@slawas.pl>
mail.data.test2.cc=
mail.data.test2.bcc=
mail.data.test2.subject=Test wysyłania maila z pustym załącznikiem
mail.data.test2.txt=Test wiadomości z załącznikiem.<br/>Polskie znaczki powinny być poprawne: ąśćęĄŚĘŃŹŻ :)<br/><b>pozdrawiam</b><br/> Slawas
mail.data.test2.attachments=src/test/resources/pusty.docx
#mail.data.test2.codePage=CP1250
mail.data.test2.codePage=iso-8859-2

#Test3 wysłanie pustego maila
mail.data.test3.from=scichy@ibpm.pro
mail.data.test3.replyTo=
mail.data.test3.to=Sławomir Cichy <slawas@slawas.pl>
mail.data.test3.cc=
mail.data.test3.bcc=
mail.data.test3.subject=Test wysyłania pustego maila
mail.data.test3.txt=
mail.data.test3.attachments=
-------------

########################
test.properties (UTF-8)
########################
-------------
# parametry dla połączenia do bazy danych
jdbc.dbHost=localhost
jdbc.dbPort=1521
jdbc.dbSID=ANDROMOBILE
jdbc.dbUser=ss
jdbc.dbPassword=ss

# parametry połączenia do skrzynki pocztowej
#----IMAP---------
#ustaw swoje dane email!
#mail.imap.server=poczta.ibpm.pl
mail.imap.server=outlook.office365.com
#mail.imap.username=scichy
mail.imap.username=scichy@ibpm.pro
mail.imap.password=secret
mail.imap.mailbox.in=Inbox
mail.imap.mailbox.out=Alerty
#mail.imap.mailbox.in=testDEV
mail.imap.mailbox.limit=5
mail.imap.mailbox.start=2
mail.imap.mailbox.timeout=10000

#----SMTP---------
#mail.smtp.host=smtp.ibpm.pro
mail.smtp.host=smtp.office365.com
mail.smtp.auth=true
#ustaw swoje dane email!
#mail.smtp.user=scichy
mail.smtp.user=scichy@ibpm.pro
mail.smtp.password=secret
mail.smtp.starttls.enable=false;
mail.smtp.port=587

#
mail.imap.forward.id=690485778.1.1443121406274.JavaMail.slawas@satelita
mail.imap.forward.id.1=20150923085109.Horde.aZYDbHs97yuKYq5IB5GbWg1@poczta.ibpm.pro
mail.imap.forward.id.2=20150922114249.Horde.1N1TUe04xJx8k3EDlI7jSA2@poczta.ibpm.pro

mail.imap.forward.to=To Sławek <slawas.cichy@gmail.com>
mail.imap.forward.replyTo=ReplyTo Sławek <slawas.cichy@gmail.com>
mail.imap.forward.from=From Sławek <scichy@ibpm.pro>
mail.imap.forward.cc=Cc Sławek <slawas.cichy@gmail.com>
mail.imap.forward.bcc=Bcc Sławek <slawas.cichy@gmail.com>

mail.imap.mailbox.send=Alerty
mail.imap.mailbox.trash=Trash
mail.imap.mailbox.draft=Draft
mail.imap.flgReturnAtt=false
mail.imap.flgScanNarrative=false
mail.imap.flgDeleteRead=false
mail.imap.partialfetch=false



pl.slawas.common.mail.ExchangeTest.testGetMessagesByID.skip=true
pl.slawas.common.mail.ExchangeTest.testGetMessagesByIDs.skip=true
pl.slawas.common.mail.ExchangeTest.testCreateMessageToFile.skip=false
pl.slawas.common.mail.ExchangeTest.testForwardMessagesByIDs.skip=false
pl.slawas.common.mail.ExchangeTest.testGetMessagePathByID.skip=true
pl.slawas.common.mail.ExchangeTest.testGetMessages.skip=true
pl.slawas.common.mail.ExchangeTest.testSendMail.skip=false
pl.slawas.common.mail.ExchangeTest.testSendMailWithEmptyDocx.skip=false
pl.slawas.common.mail.ExchangeTest.testSendEmptyMail.skip=false
pl.slawas.common.mail.helpers.MessageHelperTest.testPrepareRecipientsAddressesArray=false
pl.slawas.common.mail.helpers.MessageHelperTest.testPrepareRecipientsAddresses=false
pl.slawas.common.mail.utils.MailUtilsTest.testParseContentType=false

-------------