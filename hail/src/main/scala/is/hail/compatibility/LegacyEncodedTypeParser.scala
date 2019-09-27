package is.hail.compatibility

import is.hail.expr.ir.IRParser._
import is.hail.expr.ir.{IRParser, PunctuationToken, TokenIterator, TypeParserEnvironment}
import is.hail.expr.types.encoded._
import is.hail.expr.types.virtual._
import is.hail.utils.FastIndexedSeq

object LegacyEncodedTypeParser {

  def parse(env: TypeParserEnvironment)(it: TokenIterator): (Type, EType) = {
    val req = it.head match {
      case x: PunctuationToken if x.value == "+" =>
        consumeToken(it)
        true
      case _ => false
    }

    val (vType, eType) = identifier(it) match {
      case "Interval" =>
        punctuation(it, "[")
        val (pointType, ePointType) = parse(env)(it)
        punctuation(it, "]")
        (TInterval(pointType, req), EBaseStruct(FastIndexedSeq(
          EField("start", ePointType, 0),
          EField("end", ePointType, 1),
          EField("includesStart", EBooleanRequired, 2),
          EField("includesEnd", EBooleanRequired, 3)
        ), req))
      case "Boolean" => (TBoolean(req), EBoolean(req))
      case "Int32" => (TInt32(req), EInt32(req))
      case "Int64" => (TInt64(req), EInt64(req))
      case "Int" => (TInt32(req), EInt32(req))
      case "Float32" => (TFloat32(req), EFloat32(req))
      case "Float64" => (TFloat64(req), EFloat64(req))
      case "String" => (TString(req), EBinary(req))
      case "Locus" =>
        punctuation(it, "(")
        val rg = identifier(it)
        punctuation(it, ")")
        (env.getReferenceGenome(rg).locusType.setRequired(req), EBaseStruct(FastIndexedSeq(
          EField("contig", EBinaryRequired, 0),
          EField("position", EInt32Required, 1)), req))
      case "Call" => (TCall(req), EInt32(req))
      case "Array" =>
        punctuation(it, "[")
        val (elementType, elementEType) = parse(env)(it)
        punctuation(it, "]")
        (TArray(elementType, req), EArray(elementEType, req))
      case "Set" =>
        punctuation(it, "[")
        val (elementType, elementEType) = parse(env)(it)
        punctuation(it, "]")
        (TSet(elementType, req), EArray(elementEType, req))
      case "Dict" =>
        punctuation(it, "[")
        val (keyType, keyEType) = parse(env)(it)
        punctuation(it, ",")
        val (valueType, valueEType) = parse(env)(it)
        punctuation(it, "]")
        (TDict(keyType, valueType, req), EArray(EBaseStruct(FastIndexedSeq(
          EField("key", keyEType, 0),
          EField("value", valueEType, 1)), required = true),
          req))
      case "Tuple" =>
        punctuation(it, "[")
        val types = repsepUntil(it, parse(env), PunctuationToken(","), PunctuationToken("]"))
        punctuation(it, "]")
        (TTuple(req, types.map(_._1): _*), EBaseStruct(types.zipWithIndex.map { case ((_, t), idx) => EField(idx.toString, t, idx) }, req))
      case "Struct" =>
        punctuation(it, "{")
        val args = repsepUntil(it, struct_field(parse(env)), PunctuationToken(","), PunctuationToken("}"))
        punctuation(it, "}")
        val (vFields, eFields) = args.zipWithIndex.map { case ((id, (vt, et)), i) => (Field(id, vt, i), EField(id, et, i)) }.unzip
        (TStruct(vFields, req), EBaseStruct(eFields, req))
    }
    assert(vType.required == req)
    assert(eType.required == req)
    (vType, eType)
  }

  def apply(str: String, env: TypeParserEnvironment): (Type, EType) = {
    IRParser.parse(str, it => parse(env)(it))
  }
}
