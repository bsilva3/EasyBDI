# Easy BDI
Easy BDI is a tool for performing analytical queries over distributed and possibly heterogeneous data sources, using Presto to communicate with the data sources. Star schemas can be created and used for analytical queries.
Easy BDI was developed as a masters degree project dissertation for Computer Engineering in University of Aveiro (www.ua.pt) and was partially funded by Fundo Europeu de Desenvolvimento Regional (FEDER), Programa Operacional Competitividade e Internacionalização in the context of the project Produtech II SIF -- POCI-01-0247-FEDER-024541.



## Easy BDI main features
- Configuration of the local schema view, global schema, and star schema using a wizard-like interface.
- Support for several data sources already tested, such as MySQL, PostgreSQL, Hive, Cassandra, MongoDB and raw files (csv, tsv, txt).
- Creation of analytical queries using a drag-and-drop interface.
- Queries can be saved and re-used again and query results can be exported.

## System requirements and How to install
Easy BDI requires JRE 8. It can be installed in any Operative System. However, Presto (also known as Trino), the distributed query engine used to communicate with the data sources, is only compatible with linux distribuitions, which means that most of Easy BDI's functionalities can only be executed on a linux system, since some of them use Presto (local schema view creation, query execution)

## How to use

First, users configure the data sources to generate a local schema view, and correct the proposed global schema by Easy BDI, and finally map said global schema to a star schema by choosing the dimensions and facts tables (and its measures). Using this star schema, users can then submit anaytical queries using a drag-and-drop user interface

Easy BDI uses Presto to query each data source, therefore the ammount of data sources supported by Easy BDI is dependent on the ammount of supported data sources by Presto. Note that fully compatibility for each of these data sources on Easy BDI requires small effort coding on Easy BDI's source code.


## System demonstration

To demonstrate the system, two case studies are available for usage:
- More than 3 years of real data on photovoltaic panel production/consumption data in Sidney, Australia and nearby areas
- SSB+ benchmark – retail and streaming data containing sales, deliveries and social media popularity of retail stores

## Citation
This project was subject of a [demo paper](https://edbt2021proceedings.github.io/docs/p190.pdf) published in EDBT 2021. To cite this work, please use:
>Bruno Silva, José Moreira, Rogério Luís C. Costa (2021) EasyBDI: Near Real-Time Data Analytics over Heterogeneous Data Sources. In Proceedings of the 24th International Conference on Extending Database Technology, EDBT 2021. pp 702-705.

You can also check a [30 second presentation submitted to EDBT](https://www.youtube.com/watch?v=_RVXNxPU1dw) and an [overview poster](https://edbt2021proceedings.github.io/ads/a190.png).  
For a more in depth analysis of this work, please consult the [master thesis developed for this project](https://ria.ua.pt/handle/10773/31215)


## Aknowledgements
This work was partially funded by the European Regional Development Fund (ERDF), Operational Program Competitiveness and Internationalization in the context of the PRODUCECH SIF project – Soluções para a Indústria de Futuro – POCI-01-0247-FEDER-024541

## License

It is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

Easy BDI is distributed "AS IS" in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
