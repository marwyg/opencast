Speech to Text Workflow Operation
==============================

ID: `speechtotext`

Description
-----------

The speech to text operation can be used to generate subtitles for Videos or Audio files. Currently, there is
only Vosk as STT Engine available [Vosk](https://alphacephei.com/vosk/) and there resulting file is
in the WebVVT format.


Parameter Table
---------------

|configuration keys|required|description                                                                      |
|------------------|--------|---------------------------------------------------------------------------------|
|source-flavor     |yes     |The source media package to use                                                  |
|target-flavor     |yes     |Flavor of the produced subtitle file                                             |
|target-element    |no      |Define where to append the subtitles file. Possibilities are: as a 'track' or as an 'attachment'. The default is "attachment".|
|language-code     |no      |The language of the video or audio source (default is "eng). Has to match the name of the language model directory. See 'vosk-cli'.|
|target-tags       |no      |Tags for the subtitle file                                                       |


vosk-cli
------------

A manual for installing the vosk-cli python package can be found here: https://github.com/elan-ev/vosk-cli.


Operation Examples
------------------

```XML
<operation
    id="speechtotext"
    description="Generates subtitles for video and audio files">
  <configurations>
    <configuration key="source-flavor">*/source</configuration>
    <configuration key="target-flavor">*/subtitle</configuration>
    <configuration key="target-element"> attachment | track </configuration>
    <configuration key="target-tags">subtitle</configuration>
    <configuration key="language-code">ger</configuration>
  </configurations>
</operation>
```
