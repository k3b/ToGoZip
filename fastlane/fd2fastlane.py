#!/usr/bin/env python3

# Author: k3b
# License: GPLv3 or laterwhere

# convert [prj-root]/app/src/debug/res/values-zz/fdroid.xml
# to [prj-root]/fastlane/metadata/android/zz/*.txt

import glob
import markdown
import os
import re
from xml.etree import ElementTree

#--------------------------------------
# configuration: set constant to None if you do not want to use the featrue

# if not None replace title becomes DEFAULT_APP_TITLE (translation)
DEFAULT_APP_TITLE = "ToGoZip"

# all paths are relativ to [root]/fastlane/*.py

# mandatory: translations come from [prj]/app/src/debug/res/values-[locale]/fdroid.xml
FD_SRC_PATH_ROOT = '../app/src/debug/res'

# mandatory: fastlane data is written to [prj]/fastlane/metadata/android/*/*.txt
FASTLANE_OUT_ROOT = 'metadata/android'

# if not null markdown in full_description is written to [prj]/app/src/main/res/values-[locale]/html-pages.xml
APP_OUT_RES_ROOT = '../app/src/main/res'

# if not null markdown in full_description is written to [prj]/../AndroFotoFinder.wiki/[locale]-home.md
WIKI_OUT_ROOT = '../../ToGoZip.wiki'

DEFAULT_LANG = 'en-US'

#--------------------------------------

# translate android-locale to fastlane locale
# if not in this list defaults to 2 letter iso
# example '-eu' will become 'eu'
LANG_ANDROID_TO_FASTLANE = {
    '': 'en-US',
    '-ar': 'ar-SA',
    '-de': 'de-DE',
    '-es': 'es-ES',
    '-fr': 'fr-FR',
    '-in': 'id-ID',
    '-ja': 'ja-JP',
    '-nl': 'nl-NL',
#   '-pt-rBR': 'pt-BR',
    '-ru': 'ru-RU',
    '-uk': 'uk-UA',
    '-zh-rCN': 'zh-CN',
    '-zh-rTW': 'zh-TW',
    #
    '-cs': 'cs-CZ',
    '-el': 'el-GR',
    '-ta': 'ta-IN',
}

# translate android-locale to wiki locale
# if not in this list defaults to 2 letter iso
# example '-eu' will become 'eu'
LANG_ANDROID_TO_WIKI = {
    '': '',
    '-pt-rBR': 'pt',
    '-zh-rCN': 'zh-CN',
    '-zh-rTW': 'zh-TW',
}

PATH = os.path.dirname(os.path.realpath(__file__))
REGEX_ANDROID_LOCALE = re.compile(r'.*[/\\]values([^/\\]*)[/\\]fdroid\.xml', re.IGNORECASE)


def save_to_wiki_homepage(title, short_description, full_description, android_locale):
    if ((mylen(WIKI_OUT_ROOT) > 0) and (mylen(android_locale) > 0)):
        label = f'<!--!!generated from .../values{android_locale}/fdroid.xml!!-->';
        homepate_locale = translate_locale(android_locale, LANG_ANDROID_TO_WIKI)
        filename = homepate_locale + '-home.md'
        full_md_path = os.path.join(WIKI_OUT_ROOT, filename)
        oldContent = None
        if os.path.exists(full_md_path):
            with open(full_md_path, 'rt', encoding='UTF-8') as f:
                oldContent = f.read()
                if oldContent.find(label) >= 0:
                    # was autogenerated: overwrite
                    oldContent = None
        if oldContent is None:
            # does not exist or was autogenerated: overwrite

            md = generate_wiki_homepage(label, title, short_description, full_description)
            # print(f'\t{full_md_path[-10:]}\t{oldContent[:10]}\t{mylen(oldContent)} {label}')
            save_to_file(md, WIKI_OUT_ROOT, filename)


def generate_wiki_homepage(label, title, short_description, full_description):
    md = f"""{label}
## ![](https://raw.githubusercontent.com/k3b/ToGoZip/master/app/src/main/res/drawable-hdpi/ic_launcher.png) {title}

{short_description}

{full_description}

"""
    return md


def save_to_app_about(title, short_description, full_description, android_locale):
    if (mylen(APP_OUT_RES_ROOT) > 0) and (mylen(android_locale) > 0):
        label = f'<!--!!generated from .../values{android_locale}/fdroid.xml!!-->';
        dir = os.path.join(APP_OUT_RES_ROOT, "values" + android_locale)
        filename = "html-pages.xml"
        full_md_path = os.path.join(dir, filename)
        oldContent = None
        if os.path.exists(full_md_path):
            with open(full_md_path, 'rt', encoding='UTF-8') as f:
                oldContent = f.read()
                if oldContent.find(label) >= 0:
                    # was autogenerated: overwrite
                    oldContent = None
        if oldContent is None:
            # does not exist or was autogenerated: overwrite
            md = generate_app_about(label, title, short_description,
                                    full_description.replace("\"", "\\\""))
            # print(f'\t{full_md_path[-10:]}\t{oldContent[:10]}\t{mylen(oldContent)} {label}')
            save_to_file(md, dir, filename)


def generate_app_about(label, title, short_description, full_description):
    md = f"""<?xml version="1.0" encoding="utf-8" ?>
<!--
/* 
 * This file is part of "A Photo Manager" (A(ndro)FotoFinder).
 *
 * Localized Aboutbox within the app
 */
 -->
<resources>
  {label}
  <string name="about_content_about"><![CDATA["
<h1>{title}</h1>
<br />
<h2>{short_description}</h2>
<br />
{full_description}
<br />
<br />
<br />
<br />
  "]]></string>
</resources>
"""
    return md


def process_translation(fdroid_xml, android_locale):
    if os.path.isfile(fdroid_xml):
        fastlane_locale = translate_locale(android_locale, LANG_ANDROID_TO_FASTLANE)

        # print(android_locale + '\t' + fastlane_locale + '\t' + fdroid_xml)
        root = ElementTree.parse(fdroid_xml).getroot()
        title = getElementText(root, './/string[@name="title"]', 50, fdroid_xml)
        if mylen(DEFAULT_APP_TITLE) > 0 and mylen(title) > 0 and title.find(DEFAULT_APP_TITLE) < 0:
            title = DEFAULT_APP_TITLE + " (" + title + ")"

        short_description = getElementText(root, './/string[@name="short_description"]', 80,
                                           fdroid_xml)
        full_description = getElementText(root, './/string[@name="full_description"]', 4000,
                                          fdroid_xml)

        cur_fastlane_out_dir = os.path.join(PATH, FASTLANE_OUT_ROOT, fastlane_locale)

        save_to_file(title, cur_fastlane_out_dir, 'title.txt')
        save_to_file(short_description, cur_fastlane_out_dir, 'short_description.txt')

        if (full_description is not None):
            if mylen(title) == 0:
                title = DEFAULT_APP_TITLE

            html = markdown.markdown(full_description)
            save_to_file(html, cur_fastlane_out_dir, 'full_description.txt')
            save_to_wiki_homepage(title, short_description, full_description, android_locale)
            save_to_app_about(title, short_description, html, android_locale)


def translate_locale(android_locale, locale_map):
    result_locale = ""
    if android_locale in locale_map.keys():
        result_locale = locale_map[android_locale]
    elif (mylen(android_locale) == 3):
        # if not found: translate '-de' to 'de'
        result_locale = android_locale[1:]
    return result_locale


def getElementText(root, xml_path, limit=0, context=''):
    element = root.find(xml_path)
    if (element is not None):
        return clean_text(element.text, limit, context[-20:] + ' ' + xml_path)
    return None


def save_to_file(content, directory_path, filename):
    if (content is not None):
        file_path = os.path.join(directory_path, filename)
        # print('\t' + file_path[-30:] + '\t' + content[:60])
        if not os.path.exists(directory_path):
            os.makedirs(directory_path)
        print("Writing %s..." % file_path)
        with open(file_path, 'wt', encoding='UTF-8') as f:
            f.write(content)


def clean_text(text, limit=0, context=''):
    # remove leading/trailing blanks and string delimiter, unescape String delimiter
    text = text.strip("\s\"'").replace('\\\'', '\'').replace('\\\"', '\"')
    if limit != 0 and mylen(text) > limit:
        print(context + " Warning: Text longer than %d characters, ignoring..." % limit)
        # text = text[:limit]
    return text


def mylen(item):
    if item is None:
        return 0
    return len(item)


def main():
    path = os.path.join(PATH, FD_SRC_PATH_ROOT, 'values*/fdroid.xml')

    # path = os.path.join(path,'values[^/\\]*/fdroid.xml')
    glob.glob(path)
    for fdroid_xml in glob.glob(path):
        android_locale = REGEX_ANDROID_LOCALE.sub(r'\1', fdroid_xml)

        process_translation(fdroid_xml, android_locale)


if __name__ == "__main__":
    main()
