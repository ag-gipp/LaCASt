[![Tests](https://github.com/ag-gipp/latex-grammar/workflows/translator-build-tests/badge.svg)](https://github.com/ag-gipp/latex-grammar/workflows/translator-build-tests/badge.svg) [![Maintainability](https://api.codeclimate.com/v1/badges/3960df830b098ef0afa9/maintainability)](https://codeclimate.com/repos/5df6328a606a9501a1001189/maintainability) 

# LaCASt - A LaTeX Translator for Computer Algebra Systems

# Preface
1. This is a private repository with non-public sources. It is strictly **_prohibited to share_** without permission.
2. If you wish to contribute, read the [contribution guidelines](CONTRIBUTING.md) first.

## Structure
1. [How to use the program](#howTo)
2. [Setup Round Trip Tests and Backward Translations](#roundtrip)
3. [Team Members](#contributers)

## How to use our program<a name="howTo"></a>
The executable jar for the translator can be found in the `bin` subdirectory. If you don't need to checkout the entire
repository, you will find a `zip` file in the `bin` subdirectory that contains all necessary files to run the program.

Run the jar always in the root directory of this repository, not inside the `bin` directory. Hence, you start the program
via 
``` shell script
java -jar latex-to-cas-converter.jar
```

Without additional information, the jar will run as a client program. However, you can start the program to directly trigger
the translation process.    
* `-CAS=<NameOfCAS>`: Sets the computer algebra system you want to use. For instance `-CAS=Maple` uses Maple or `-CAS=Mathematica` uses Mathematica. If you don't set this flag, the program will ask you which CAS you want to use.
* `-Expression="<exp>"`: Sets the expression you want to translate. Make sure you don't forget the quotation marks. If you don't specify an expression, the program will ask you about it.
* `--clean` or `-c`: Only returns the translated expression without any other information. (since version 1.0.1)
* `--debug` or `-d`: Returns extra information for debugging, such as computation time and list of elements. The `--clean` flag would override this effect.
* `--extra` or `-x`: Shows further information about translation of functions. Like branch cuts, DLMF-links and so on. The `--clean` flag would override this effect.

The translation patterns are defined in `libs/ReferenceData/CSVTables`. If you wish to add translation patterns you need to
compile the changes before the translator can use them. To update the translations, use the `lexicon-creator.jar`.
* `lexicon-creator.jar`: This jar takes the CSV files in `libs/ReferenceData/CSVTables` and translate them to a lexicon 
file (the math language processors based on this lexicon files). Running this jar will ask you to enter the names of 
the CAS you want to update (one CAS per line). If you want to update all at once, just enter `all`. For further information check 
_[contribution guidelines](CONTRIBUTING.md)_.

#### Custom Replacements
The pre-processing replacement rules are defined `config/replacements.yml` and `config/dlmf-replacements.yml`. Each config
contains further explanations how to add replacement rules. The replacement rules are applied without further compilation.
Just change the files to add, modify, or remove rules.

## Round Trip Test Setup<a name="roundtrip"></a>
To run the round trip tests (and symbolic as well as numerical tests), you will find executable jars in the `bin` directory
for all of the tests. The tests require some settings. 

For the tests you have to specify environment variables in order to use CAS engines.
* [config/maple_config.properties](config/maple_config.properties): sets the maple bin directory
``` properties
maple_bin=/opt/maple2016/bin.X86_64_LINUX
```
* [config/mathematica_config.properties](config/mathematica_config.properties): sets the mathematica bin directory
``` properties
mathematica_math=/opt/Wolfram/Executables/math
```

Further you have to set system environment variables.
```
export LD_LIBRARY_PATH = "$LD_LIBRARY_PATH:<Maple-BinDir>:<Mathematica-LibDir>"
export MAPLE="<Maple-Directory>"
```
Where 
* `<Maple-Directory>` points to the directory where you installed your Maple version, e.g., `/opt/maple2016`. 
* `<Maple-BinDir>` points to the binary folder of your installed Maple version, e.g., `/opt/maple2016/bin.X86_64_LINUX`.
* `<Mathematica-LibDir>` points to the system library directory of the Mathematica installation, e.g., 
`/opt/Wolfram/SystemFiles/Links/JLink/SystemFiles/Libraries/Linux-x86-64`. Note that the final directory is system 
dependent, e.g., for Windows it should be `Windows`.

You can fetch the information from Maple when you enter the following commands
```
kernelopts( bindir );   <- returns <Maple-BinDir>
kernelopts( mapledir ); <- returns <Maple-Directory>
```

## Contributors<a name="contributers"></a>

| Role | Name | Contact |
| :---: | :---: | :---: |
| **Main Developer** | [André Greiner-Petter](https://github.com/AndreG-P) | [andre.greiner-petter@t-online.de](mailto:andre.greiner-petter@t-online.de) |
| **Supervisor** | [Dr. Howard Cohl](https://github.com/HowardCohl) | [howard.cohl@nist.gov](mailto:howard.cohl@nist.gov) |
| **Advisor** | [Dr. Moritz Schubotz](https://github.com/physikerwelt) | [schubotz@uni-wuppertal.de](mailto:schubotz@uni-wuppertal.de) |
| **Advisor** | [Prof. Abdou Youssef](https://github.com/abdouyoussef) | [abdou.youssef@nist.gov](mailto:abdou.youssef@nist.gov) |
| **Student Developers** | [Avi Trost](https://github.com/avitrost) & [Rajen Dey](https://github.com/Nejiv) & [Claude](https://github.com/ClaudeZou) & [Jagan](https://github.com/notjagan) | |
