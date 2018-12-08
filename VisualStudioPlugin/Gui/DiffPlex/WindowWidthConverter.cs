using System;
using System.Globalization;
using System.Windows.Data;

namespace DSLPlatform
{
	// Hack to stop TextBoxes resizing based on their value!
	// I'm binding DiffPlexControl's Width to ActualWidth of Window. The problem is Window's ActualWidth is for about 30 pixels larger than I can use in application. This converter "fixes" it.
	// "Better" solutions I found on internet didn't work
	internal class WindowWidthConverter : IValueConverter
	{
		public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
		{
			if (value is double)
				return Math.Max((double)value - 40d, 0d);

			return 0.0d;
		}

		public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
		{
			throw new NotImplementedException();
		}
	}
}
